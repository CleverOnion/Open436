use actix_web::{web, HttpResponse};
use serde::{Deserialize, Serialize};
use sqlx::PgPool;
use std::sync::Arc;
use uuid::Uuid;

use crate::models::FileResponse;
use crate::services::FileService;
use crate::storage::StorageBackend;
use crate::utils::error::FileError;

#[derive(Debug, Deserialize)]
pub struct BatchInfoRequest {
    pub file_ids: Vec<String>,
}

#[derive(Debug, Serialize)]
pub struct ApiResponse<T> {
    pub code: u32,
    pub message: String,
    pub data: T,
    pub timestamp: String,
}

#[derive(Debug, Serialize)]
pub struct BatchInfoResponse {
    pub files: Vec<FileResponse>,
    pub total: usize,
}

const MAX_BATCH_SIZE: usize = 100;

/// 批量获取文件信息
pub async fn batch_info_handler(
    payload: web::Json<BatchInfoRequest>,
    pool: web::Data<PgPool>,
    storage: web::Data<Arc<dyn StorageBackend>>,
) -> Result<HttpResponse, FileError> {
    // 1. 验证请求
    if payload.file_ids.is_empty() {
        return Err(FileError::EmptyIdList);
    }

    if payload.file_ids.len() > MAX_BATCH_SIZE {
        return Err(FileError::TooManyIds {
            count: payload.file_ids.len(),
            max: MAX_BATCH_SIZE,
        });
    }

    // 2. 解析 UUID
    let mut uuids = Vec::new();
    for id_str in &payload.file_ids {
        let uuid = Uuid::parse_str(id_str)
            .map_err(|_| FileError::InvalidUuid(id_str.to_string()))?;
        uuids.push(uuid);
    }

    // 3. 批量查询
    let files = FileService::get_files_by_ids(&pool, &uuids)
        .await
        .map_err(|e| FileError::Database(e))?;

    // 4. 生成响应
    let mut file_responses = Vec::new();
    for file in files {
        let url = storage
            .get_url(&file.storage_key)
            .await
            .map_err(|e| FileError::Storage(e.to_string()))?;
        file_responses.push(FileResponse::from((file, url)));
    }

    let response = ApiResponse {
        code: 200,
        message: "Success".to_string(),
        data: BatchInfoResponse {
            total: file_responses.len(),
            files: file_responses,
        },
        timestamp: chrono::Utc::now().to_rfc3339(),
    };

    Ok(HttpResponse::Ok().json(response))
}

