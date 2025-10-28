use actix_web::{web, HttpResponse};
use serde::Serialize;
use sqlx::PgPool;
use std::collections::HashMap;

#[derive(Debug, Serialize)]
pub struct HealthResponse {
    pub status: String,
    pub service: String,
    pub version: String,
    pub timestamp: String,
    pub checks: HashMap<String, String>,
}

/// 健康检查
pub async fn health_handler(pool: web::Data<PgPool>) -> HttpResponse {
    let mut checks = HashMap::new();

    // 检查数据库连接
    match crate::db::health_check(&pool).await {
        Ok(_) => {
            checks.insert("database".to_string(), "ok".to_string());
        }
        Err(e) => {
            checks.insert("database".to_string(), format!("error: {}", e));
        }
    }

    let all_ok = checks.values().all(|v| v == "ok");
    let status = if all_ok { "ok" } else { "error" };

    let response = HealthResponse {
        status: status.to_string(),
        service: "file-service".to_string(),
        version: env!("CARGO_PKG_VERSION").to_string(),
        timestamp: chrono::Utc::now().to_rfc3339(),
        checks,
    };

    if all_ok {
        HttpResponse::Ok().json(response)
    } else {
        HttpResponse::ServiceUnavailable().json(response)
    }
}

