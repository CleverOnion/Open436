use actix_web::{web, HttpResponse};
use serde::Serialize;
use sqlx::PgPool;
use std::sync::Arc;
use uuid::Uuid;

use crate::models::FileResponse;
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

/// 获取文件信息
pub async fn get_file_info_handler(
    file_id: web::Path<String>,
    pool: web::Data<PgPool>,
    storage: web::Data<Arc<dyn StorageBackend>>,
) -> Result<HttpResponse, FileError> {
    // 解析 UUID
    let uuid = Uuid::parse_str(&file_id)
        .map_err(|_| FileError::InvalidUuid(file_id.to_string()))?;

    // 查询文件
    let file = FileService::get_file_by_id(&pool, uuid).await?;

    // 获取 URL
    let url = storage
        .get_url(&file.storage_key)
        .await
        .map_err(|e| FileError::Storage(e.to_string()))?;

    let file_response = FileResponse::from((file, url));

    let response = ApiResponse {
        code: 200,
        message: "Success".to_string(),
        data: file_response,
        timestamp: chrono::Utc::now().to_rfc3339(),
    };

    Ok(HttpResponse::Ok().json(response))
}

/// 获取文件 URL
pub async fn get_file_url_handler(
    file_id: web::Path<String>,
    pool: web::Data<PgPool>,
    storage: web::Data<Arc<dyn StorageBackend>>,
) -> Result<HttpResponse, FileError> {
    // 解析 UUID
    let uuid = Uuid::parse_str(&file_id)
        .map_err(|_| FileError::InvalidUuid(file_id.to_string()))?;

    // 查询文件
    let file = FileService::get_file_by_id(&pool, uuid).await?;

    // 获取 URL
    let url = storage
        .get_url(&file.storage_key)
        .await
        .map_err(|e| FileError::Storage(e.to_string()))?;

    #[derive(Serialize)]
    struct UrlData {
        file_id: String,
        url: String,
    }

    let response = ApiResponse {
        code: 200,
        message: "Success".to_string(),
        data: UrlData {
            file_id: file.id.to_string(),
            url,
        },
        timestamp: chrono::Utc::now().to_rfc3339(),
    };

    Ok(HttpResponse::Ok().json(response))
}

