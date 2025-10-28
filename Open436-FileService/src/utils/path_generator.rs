use crate::utils::error::FileError;
use anyhow::Result;
use chrono::{Datelike, Utc};
use std::path::Path;
use uuid::Uuid;

/// 文件路径生成器
pub struct PathGenerator;

impl PathGenerator {
    /// 生成安全的存储路径
    /// 格式: YYYY/MM/DD/uuid.ext
    pub fn generate_storage_key(original_filename: &str) -> Result<String, FileError> {
        // 1. 提取扩展名
        let ext = Path::new(original_filename)
            .extension()
            .and_then(|e| e.to_str())
            .ok_or_else(|| FileError::InvalidFileName("Invalid filename".to_string()))?
            .to_lowercase();

        // 2. 验证扩展名
        if !["jpg", "jpeg", "png", "gif", "svg"].contains(&ext.as_str()) {
            return Err(FileError::UnsupportedFileType(format!(
                "Extension '{}' not supported",
                ext
            )));
        }

        // 3. 生成 UUID 文件名
        let uuid = Uuid::new_v4();
        let filename = format!("{}.{}", uuid, ext);

        // 4. 按日期分目录（YYYY/MM/DD）
        let now = Utc::now();
        let year = now.year();
        let month = now.month();
        let day = now.day();

        let storage_key = format!("{:04}/{:02}/{:02}/{}", year, month, day, filename);

        Ok(storage_key)
    }

    /// 清理原始文件名（用于保存到数据库）
    pub fn sanitize_filename(filename: &str) -> String {
        filename
            .chars()
            .filter(|c| c.is_alphanumeric() || *c == '.' || *c == '-' || *c == '_' || *c == ' ')
            .take(255)
            .collect()
    }

    /// 验证存储路径不包含危险字符
    pub fn validate_storage_key(key: &str) -> Result<(), FileError> {
        // 检查路径遍历
        if key.contains("..") {
            return Err(FileError::PathTraversal);
        }

        // 检查绝对路径
        if key.starts_with('/') || key.starts_with('\\') {
            return Err(FileError::PathTraversal);
        }

        // 检查 Windows 驱动器路径
        if key.len() >= 2 && key.chars().nth(1) == Some(':') {
            return Err(FileError::PathTraversal);
        }

        Ok(())
    }

    /// 检查路径安全性
    pub fn is_safe_path(path: &str) -> bool {
        // 禁止的模式
        let forbidden = [
            "..",      // 父目录
            "//",      // 多重斜杠
            "\\\\",    // Windows 路径
            "\0",      // 空字节
            "|", "&", ";", // Shell 命令
        ];

        for pattern in &forbidden {
            if path.contains(pattern) {
                return false;
            }
        }

        // 路径必须是相对路径
        if path.starts_with('/') || path.starts_with('\\') {
            return false;
        }

        // Windows 驱动器路径检查
        if path.len() >= 2 {
            let chars: Vec<char> = path.chars().collect();
            if chars.get(1) == Some(&':') {
                return false;
            }
        }

        true
    }
}

/// 格式化文件大小
pub fn format_size(bytes: i64) -> String {
    const KB: i64 = 1024;
    const MB: i64 = KB * 1024;
    const GB: i64 = MB * 1024;

    if bytes >= GB {
        format!("{:.2} GB", bytes as f64 / GB as f64)
    } else if bytes >= MB {
        format!("{:.2} MB", bytes as f64 / MB as f64)
    } else if bytes >= KB {
        format!("{:.2} KB", bytes as f64 / KB as f64)
    } else {
        format!("{} bytes", bytes)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_sanitize_filename() {
        assert_eq!(
            PathGenerator::sanitize_filename("my<script>photo.jpg"),
            "myscriptphoto.jpg"
        );
        assert_eq!(
            PathGenerator::sanitize_filename("test file.png"),
            "test file.png"
        );
    }

    #[test]
    fn test_is_safe_path() {
        assert!(PathGenerator::is_safe_path("2025/10/28/file.jpg"));
        assert!(!PathGenerator::is_safe_path("../../../etc/passwd"));
        assert!(!PathGenerator::is_safe_path("/etc/passwd"));
        assert!(!PathGenerator::is_safe_path("C:\\Windows\\System32"));
    }

    #[test]
    fn test_format_size() {
        assert_eq!(format_size(1024), "1.00 KB");
        assert_eq!(format_size(1_048_576), "1.00 MB");
        assert_eq!(format_size(1_073_741_824), "1.00 GB");
    }
}

