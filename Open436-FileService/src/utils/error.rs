use actix_web::{error::ResponseError, http::StatusCode, HttpResponse};
use serde::{Deserialize, Serialize};
use thiserror::Error;

/// 文件服务错误类型
#[derive(Error, Debug)]
pub enum FileError {
    #[error("File type not supported: {0}")]
    UnsupportedFileType(String),

    #[error("File too large: {size} bytes (max: {max} bytes)")]
    FileTooLarge { size: usize, max: usize },

    #[error("File is empty")]
    EmptyFile,

    #[error("Invalid file name: {0}")]
    InvalidFileName(String),

    #[error("Invalid file type parameter: {0}")]
    InvalidFileTypeParam(String),

    #[error("Path traversal detected")]
    PathTraversal,

    #[error("File not found: {0}")]
    FileNotFound(String),

    #[error("File usage not found")]
    UsageNotFound,

    #[error("File already marked as used for this usage")]
    AlreadyMarked,

    #[error("Authentication required")]
    Unauthorized,

    #[error("Admin permission required")]
    Forbidden,

    #[error("Too many file IDs: {count} (max: {max})")]
    TooManyIds { count: usize, max: usize },

    #[error("Empty file IDs list")]
    EmptyIdList,

    #[error("Invalid UUID: {0}")]
    InvalidUuid(String),

    #[error("Database error: {0}")]
    Database(#[from] sqlx::Error),

    #[error("Storage error: {0}")]
    Storage(String),

    #[error("Internal server error: {0}")]
    Internal(String),
}

/// API 错误响应
#[derive(Debug, Serialize, Deserialize)]
pub struct ErrorResponse {
    pub code: u32,
    pub message: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub data: Option<serde_json::Value>,
    pub timestamp: String,
}

impl FileError {
    /// 获取错误码
    pub fn error_code(&self) -> u32 {
        match self {
            // 文件上传 (7000 1xxx)
            Self::UnsupportedFileType(_) => 70001001,
            Self::InvalidFileTypeParam(_) => 70001002,
            Self::FileTooLarge { .. } => 70001003,
            Self::EmptyFile => 70001004,
            Self::InvalidFileName(_) => 70001005,
            Self::Unauthorized => 70001006,
            Self::Storage(_) => 70001007,

            // 文件查询 (7000 2xxx)
            Self::FileNotFound(_) => 70002001,
            Self::InvalidUuid(_) => 70002002,

            // 标记已使用 (7000 3xxx)
            Self::AlreadyMarked => 70003004,

            // 标记未使用 (7000 4xxx)
            Self::UsageNotFound => 70004002,

            // 文件删除 (7000 5xxx)
            Self::Forbidden => 70005002,

            // 批量操作 (7000 6xxx)
            Self::EmptyIdList => 70006001,
            Self::TooManyIds { .. } => 70006002,

            // 路径安全
            Self::PathTraversal => 70000004,

            // 其他
            Self::Database(_) => 70009001,
            Self::Internal(_) => 70009999,
        }
    }
}

impl ResponseError for FileError {
    fn status_code(&self) -> StatusCode {
        match self {
            Self::UnsupportedFileType(_) => StatusCode::BAD_REQUEST,
            Self::InvalidFileTypeParam(_) => StatusCode::BAD_REQUEST,
            Self::FileTooLarge { .. } => StatusCode::PAYLOAD_TOO_LARGE,
            Self::EmptyFile => StatusCode::BAD_REQUEST,
            Self::InvalidFileName(_) => StatusCode::BAD_REQUEST,
            Self::InvalidUuid(_) => StatusCode::BAD_REQUEST,
            Self::PathTraversal => StatusCode::FORBIDDEN,
            Self::FileNotFound(_) => StatusCode::NOT_FOUND,
            Self::UsageNotFound => StatusCode::NOT_FOUND,
            Self::AlreadyMarked => StatusCode::CONFLICT,
            Self::Unauthorized => StatusCode::UNAUTHORIZED,
            Self::Forbidden => StatusCode::FORBIDDEN,
            Self::TooManyIds { .. } => StatusCode::BAD_REQUEST,
            Self::EmptyIdList => StatusCode::BAD_REQUEST,
            Self::Database(_) => StatusCode::INTERNAL_SERVER_ERROR,
            Self::Storage(_) => StatusCode::INTERNAL_SERVER_ERROR,
            Self::Internal(_) => StatusCode::INTERNAL_SERVER_ERROR,
        }
    }

    fn error_response(&self) -> HttpResponse {
        let error_response = ErrorResponse {
            code: self.error_code(),
            message: self.to_string(),
            data: None,
            timestamp: chrono::Utc::now().to_rfc3339(),
        };

        HttpResponse::build(self.status_code()).json(error_response)
    }
}

