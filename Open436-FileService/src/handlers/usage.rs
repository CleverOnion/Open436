use actix_web::{web, HttpRequest, HttpResponse};
use serde::Serialize;
use sqlx::PgPool;
use uuid::Uuid;

use crate::models::{MarkUnusedRequest, MarkUsedRequest};
use crate::services::UsageService;
use crate::utils::error::FileError;

#[derive(Debug, Serialize)]
pub struct ApiResponse<T> {
    pub code: u32,
    pub message: String,
    pub data: T,
    pub timestamp: String,
}

#[derive(Debug, Serialize)]
pub struct MarkUsedResponse {
    pub file_id: String,
    pub status: String,
    pub usage: UsageData,
}

#[derive(Debug, Serialize)]
pub struct UsageData {
    pub usage_type: String,
    pub usage_id: i32,
    pub created_at: String,
}

#[derive(Debug, Serialize)]
pub struct MarkUnusedResponse {
    pub file_id: String,
    pub status: String,
    pub remaining_usages: i32,
}

/// 标记文件已使用
pub async fn mark_used_handler(
    _req: HttpRequest,
    file_id: web::Path<String>,
    payload: web::Json<MarkUsedRequest>,
    pool: web::Data<PgPool>,
) -> Result<HttpResponse, FileError> {
    // 解析 UUID
    let uuid = Uuid::parse_str(&file_id)
        .map_err(|_| FileError::InvalidUuid(file_id.to_string()))?;

    // 标记已使用
    let usage =
        UsageService::mark_used(&pool, uuid, &payload.usage_type, payload.usage_id).await?;

    let response = ApiResponse {
        code: 200,
        message: "File marked as used".to_string(),
        data: MarkUsedResponse {
            file_id: uuid.to_string(),
            status: "used".to_string(),
            usage: UsageData {
                usage_type: usage.usage_type,
                usage_id: usage.usage_id,
                created_at: usage.created_at.format("%Y-%m-%dT%H:%M:%SZ").to_string(),
            },
        },
        timestamp: chrono::Utc::now().to_rfc3339(),
    };

    Ok(HttpResponse::Ok().json(response))
}

/// 标记文件未使用
pub async fn mark_unused_handler(
    _req: HttpRequest,
    file_id: web::Path<String>,
    payload: web::Json<MarkUnusedRequest>,
    pool: web::Data<PgPool>,
) -> Result<HttpResponse, FileError> {
    // 解析 UUID
    let uuid = Uuid::parse_str(&file_id)
        .map_err(|_| FileError::InvalidUuid(file_id.to_string()))?;

    // 标记未使用
    let remaining_usages =
        UsageService::mark_unused(&pool, uuid, &payload.usage_type, payload.usage_id).await?;

    let status = if remaining_usages == 0 {
        "unused"
    } else {
        "used"
    };

    let response = ApiResponse {
        code: 200,
        message: "File marked as unused".to_string(),
        data: MarkUnusedResponse {
            file_id: uuid.to_string(),
            status: status.to_string(),
            remaining_usages,
        },
        timestamp: chrono::Utc::now().to_rfc3339(),
    };

    Ok(HttpResponse::Ok().json(response))
}

