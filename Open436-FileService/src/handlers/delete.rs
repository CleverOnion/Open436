use actix_web::{web, HttpRequest, HttpResponse};
use serde::Serialize;
use sqlx::PgPool;
use std::sync::Arc;
use uuid::Uuid;

use crate::middleware::auth::require_admin;
use crate::services::FileService;
use crate::storage::StorageBackend;
use crate::utils::error::FileError;

#[derive(Debug, Serialize)]
pub struct ApiResponse<T> {
    pub code: u32,
    pub message: String,
    pub data: T,
    pub timestamp: String,
}

#[derive(Debug, Serialize)]
pub struct DeleteResponse {
    pub file_id: String,
    pub deleted_at: String,
}

/// 删除文件（管理员）
pub async fn delete_file_handler(
    req: HttpRequest,
    file_id: web::Path<String>,
    pool: web::Data<PgPool>,
    storage: web::Data<Arc<dyn StorageBackend>>,
) -> Result<HttpResponse, FileError> {
    // 1. 验证管理员权限
    require_admin(&req)?;

    // 2. 解析 UUID
    let uuid = Uuid::parse_str(&file_id)
        .map_err(|_| FileError::InvalidUuid(file_id.to_string()))?;

    // 3. 获取文件信息
    let file = FileService::get_file_by_id(&pool, uuid).await?;

    // 4. 从存储后端删除物理文件
    storage
        .delete(&file.storage_key)
        .await
        .map_err(|e| FileError::Storage(e.to_string()))?;

    // 5. 更新数据库状态
    FileService::mark_file_deleted(&pool, uuid)
        .await
        .map_err(|e| FileError::Database(e))?;

    // 6. 返回响应
    let response = ApiResponse {
        code: 200,
        message: "File deleted successfully".to_string(),
        data: DeleteResponse {
            file_id: uuid.to_string(),
            deleted_at: chrono::Utc::now().to_rfc3339(),
        },
        timestamp: chrono::Utc::now().to_rfc3339(),
    };

    Ok(HttpResponse::Ok().json(response))
}

