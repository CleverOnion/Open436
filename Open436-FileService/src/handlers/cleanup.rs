use actix_web::{web, HttpRequest, HttpResponse};
use serde::{Deserialize, Serialize};
use sqlx::PgPool;
use std::sync::Arc;

use crate::config::CleanupConfig;
use crate::middleware::auth::require_admin;
use crate::services::CleanupService;
use crate::storage::StorageBackend;
use crate::utils::error::FileError;

#[derive(Debug, Deserialize)]
pub struct CleanupRequest {
    #[serde(default)]
    pub dry_run: bool,
}

#[derive(Debug, Serialize)]
pub struct ApiResponse<T> {
    pub code: u32,
    pub message: String,
    pub data: T,
    pub timestamp: String,
}

/// 手动触发清理（管理员）
pub async fn cleanup_handler(
    req: HttpRequest,
    payload: web::Json<CleanupRequest>,
    pool: web::Data<PgPool>,
    storage: web::Data<Arc<dyn StorageBackend>>,
    cleanup_config: web::Data<CleanupConfig>,
) -> Result<HttpResponse, FileError> {
    // 验证管理员权限
    require_admin(&req)?;

    // 执行清理
    let result = CleanupService::cleanup_unused_files(
        &pool,
        &storage,
        cleanup_config.days_threshold,
        cleanup_config.batch_size,
        payload.dry_run,
    )
    .await
    .map_err(|e| FileError::Internal(e.to_string()))?;

    let response = ApiResponse {
        code: 200,
        message: "Cleanup completed".to_string(),
        data: result,
        timestamp: chrono::Utc::now().to_rfc3339(),
    };

    Ok(HttpResponse::Ok().json(response))
}

