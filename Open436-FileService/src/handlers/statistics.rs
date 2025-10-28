use actix_web::{web, HttpRequest, HttpResponse};
use serde::Serialize;
use sqlx::PgPool;

use crate::middleware::auth::require_admin;
use crate::services::StatisticsService;
use crate::utils::error::FileError;

#[derive(Debug, Serialize)]
pub struct ApiResponse<T> {
    pub code: u32,
    pub message: String,
    pub data: T,
    pub timestamp: String,
}

/// 获取存储统计（管理员）
pub async fn statistics_handler(
    req: HttpRequest,
    pool: web::Data<PgPool>,
) -> Result<HttpResponse, FileError> {
    // 验证管理员权限
    require_admin(&req)?;

    // 获取统计数据
    let stats = StatisticsService::get_statistics(&pool)
        .await
        .map_err(|e| FileError::Internal(e.to_string()))?;

    let response = ApiResponse {
        code: 200,
        message: "Success".to_string(),
        data: stats,
        timestamp: chrono::Utc::now().to_rfc3339(),
    };

    Ok(HttpResponse::Ok().json(response))
}

