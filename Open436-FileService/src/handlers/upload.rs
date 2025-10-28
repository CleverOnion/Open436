use actix_multipart::Multipart;
use actix_web::{web, HttpRequest, HttpResponse};
use futures_util::StreamExt;
use serde::Serialize;
use sqlx::PgPool;
use std::sync::Arc;

use crate::middleware::auth::get_current_user_id;
use crate::models::{CreateFileRequest, FileType};
use crate::services::FileService;
use crate::storage::StorageBackend;
use crate::utils::error::FileError;
use crate::utils::{path_generator::PathGenerator, validator::FileValidator};

#[derive(Debug, Serialize)]
pub struct UploadResponse {
    pub code: u32,
    pub message: String,
    pub data: UploadData,
    pub timestamp: String,
}

#[derive(Debug, Serialize)]
pub struct UploadData {
    pub file_id: String,
    pub filename: String,
    pub storage_key: String,
    pub file_type: String,
    pub mime_type: String,
    pub file_size: i64,
    pub url: String,
    pub status: String,
    pub created_at: String,
}

/// 上传文件处理器
pub async fn upload_handler(
    req: HttpRequest,
    mut payload: Multipart,
    storage: web::Data<Arc<dyn StorageBackend>>,
    pool: web::Data<PgPool>,
) -> Result<HttpResponse, FileError> {
    // 1. 获取用户 ID
    let user_id = get_current_user_id(&req)?;

    let mut file_data: Vec<u8> = Vec::new();
    let mut file_type_str = String::new();
    let mut filename = String::new();

    // 2. 解析 multipart 表单
    while let Some(item) = payload.next().await {
        let mut field = item.map_err(|e| FileError::Internal(e.to_string()))?;
        let field_name = field.name().to_string();

        match field_name.as_str() {
            "file" => {
                // 获取文件名
                filename = field
                    .content_disposition()
                    .get_filename()
                    .unwrap_or("unknown")
                    .to_string();

                // 流式读取文件数据
                while let Some(chunk) = field.next().await {
                    let chunk = chunk.map_err(|e| FileError::Internal(e.to_string()))?;
                    file_data.extend_from_slice(&chunk);
                }
            }
            "file_type" => {
                while let Some(chunk) = field.next().await {
                    let chunk = chunk.map_err(|e| FileError::Internal(e.to_string()))?;
                    file_type_str.push_str(&String::from_utf8_lossy(&chunk));
                }
            }
            _ => {}
        }
    }

    // 3. 验证参数
    if file_data.is_empty() {
        return Err(FileError::EmptyFile);
    }

    if file_type_str.is_empty() {
        return Err(FileError::InvalidFileTypeParam(
            "file_type is required".to_string(),
        ));
    }

    // 4. 解析文件类型
    let file_type = file_type_str
        .parse::<FileType>()
        .map_err(|e| FileError::InvalidFileTypeParam(e))?;

    // 5. 验证文件
    FileValidator::validate_file_size(&file_data, file_type)?;
    let mime_type = FileValidator::validate_file_type(&file_data)?;
    FileValidator::validate_mime_type(&mime_type, file_type)?;

    // 6. 生成存储路径
    let storage_key = PathGenerator::generate_storage_key(&filename)?;

    // 7. 上传到存储后端
    let url = storage
        .upload(&storage_key, file_data.clone(), &mime_type)
        .await
        .map_err(|e| FileError::Storage(e.to_string()))?;

    // 8. 保存到数据库
    let safe_filename = PathGenerator::sanitize_filename(&filename);
    let create_req = CreateFileRequest {
        filename: safe_filename.clone(),
        storage_key: storage_key.clone(),
        file_type,
        mime_type: mime_type.clone(),
        file_size: file_data.len() as i64,
        uploader_id: user_id,
    };

    let file = FileService::create_file(&pool, create_req)
        .await
        .map_err(|e| FileError::Database(e))?;

    // 9. 返回响应
    let response = UploadResponse {
        code: 201,
        message: "File uploaded successfully".to_string(),
        data: UploadData {
            file_id: file.id.to_string(),
            filename: file.filename,
            storage_key: file.storage_key,
            file_type: file.file_type.to_string(),
            mime_type: file.mime_type,
            file_size: file.file_size,
            url,
            status: file.status.to_string(),
            created_at: file.created_at.format("%Y-%m-%dT%H:%M:%SZ").to_string(),
        },
        timestamp: chrono::Utc::now().to_rfc3339(),
    };

    Ok(HttpResponse::Created().json(response))
}

