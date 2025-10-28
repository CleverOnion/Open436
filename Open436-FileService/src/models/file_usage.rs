use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

/// 文件使用关联模型
#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
pub struct FileUsage {
    pub id: i32,
    pub file_id: Uuid,
    pub usage_type: String,
    pub usage_id: i32,
    pub created_at: NaiveDateTime,
}

/// 标记文件已使用请求
#[derive(Debug, Deserialize)]
pub struct MarkUsedRequest {
    pub usage_type: String,
    pub usage_id: i32,
}

/// 标记文件未使用请求
#[derive(Debug, Deserialize)]
pub struct MarkUnusedRequest {
    pub usage_type: String,
    pub usage_id: i32,
}

