use chrono::NaiveDateTime;
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use uuid::Uuid;

use super::enums::{FileStatus, FileType};

/// 文件元数据模型
#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
pub struct File {
    pub id: Uuid,
    pub filename: String,
    pub storage_key: String,
    #[sqlx(rename = "file_type")]
    pub file_type: FileType,
    pub mime_type: String,
    pub file_size: i64,
    pub uploader_id: i32,
    pub status: FileStatus,
    pub created_at: NaiveDateTime,
    pub updated_at: NaiveDateTime,
}

/// 创建文件请求
#[derive(Debug, Deserialize)]
pub struct CreateFileRequest {
    pub filename: String,
    pub storage_key: String,
    pub file_type: FileType,
    pub mime_type: String,
    pub file_size: i64,
    pub uploader_id: i32,
}

/// 文件信息响应
#[derive(Debug, Serialize)]
pub struct FileResponse {
    pub file_id: Uuid,
    pub filename: String,
    pub storage_key: String,
    pub file_type: FileType,
    pub mime_type: String,
    pub file_size: i64,
    pub url: String,
    pub status: FileStatus,
    pub uploader_id: i32,
    pub created_at: String,  // 序列化为字符串
    pub updated_at: String,
}

impl From<(File, String)> for FileResponse {
    fn from((file, url): (File, String)) -> Self {
        Self {
            file_id: file.id,
            filename: file.filename,
            storage_key: file.storage_key,
            file_type: file.file_type,
            mime_type: file.mime_type,
            file_size: file.file_size,
            url,
            status: file.status,
            uploader_id: file.uploader_id,
            created_at: file.created_at.format("%Y-%m-%dT%H:%M:%SZ").to_string(),
            updated_at: file.updated_at.format("%Y-%m-%dT%H:%M:%SZ").to_string(),
        }
    }
}

