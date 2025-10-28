use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;

/// 清理日志模型
#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
pub struct CleanupLog {
    pub id: i32,
    pub cleanup_time: NaiveDateTime,
    pub files_deleted: i32,
    pub space_freed: i64,
    pub duration_ms: Option<i32>,
    pub status: String,
    pub error_message: Option<String>,
}

/// 清理结果
#[derive(Debug, Clone, Serialize)]
pub struct CleanupResult {
    pub files_deleted: i32,
    pub space_freed: i64,
    pub space_freed_pretty: String,
    pub duration_ms: i64,
    pub status: String,
    #[serde(skip_serializing_if = "Vec::is_empty")]
    pub errors: Vec<String>,
}

