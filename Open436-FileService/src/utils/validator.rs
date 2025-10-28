use crate::models::enums::FileType;
use crate::utils::error::FileError;

/// 文件验证器
pub struct FileValidator;

impl FileValidator {
    /// 验证文件类型（基于魔数）
    pub fn validate_file_type(data: &[u8]) -> Result<String, FileError> {
        if data.is_empty() {
            return Err(FileError::EmptyFile);
        }

        // 使用 infer 库检测文件真实类型
        let kind = infer::get(data).ok_or_else(|| {
            FileError::UnsupportedFileType("Unable to detect file type".to_string())
        })?;

        // 检查是否为支持的图片类型
        match kind.mime_type() {
            "image/jpeg" | "image/png" | "image/gif" => Ok(kind.mime_type().to_string()),
            "image/svg+xml" | "text/xml" => {
                // SVG 特殊处理（可能被识别为 text/xml）
                if Self::is_svg(data) {
                    Ok("image/svg+xml".to_string())
                } else {
                    Err(FileError::UnsupportedFileType(kind.mime_type().to_string()))
                }
            }
            _ => Err(FileError::UnsupportedFileType(kind.mime_type().to_string())),
        }
    }

    /// 检查是否为 SVG
    fn is_svg(data: &[u8]) -> bool {
        let content = String::from_utf8_lossy(data);
        content.trim_start().starts_with("<svg") || content.contains("<svg")
    }

    /// 验证 MIME 类型与文件类型匹配
    pub fn validate_mime_type(mime_type: &str, file_type: FileType) -> Result<(), FileError> {
        let allowed_types = match file_type {
            FileType::Avatar | FileType::Post | FileType::Reply | FileType::SectionIcon => {
                vec!["image/jpeg", "image/png", "image/gif", "image/svg+xml"]
            }
        };

        if allowed_types.contains(&mime_type) {
            Ok(())
        } else {
            Err(FileError::UnsupportedFileType(format!(
                "MIME type '{}' not allowed for file type {:?}",
                mime_type, file_type
            )))
        }
    }

    /// 获取文件类型的大小限制（字节）
    pub fn get_size_limit(file_type: FileType) -> usize {
        match file_type {
            FileType::Avatar => 2 * 1024 * 1024,        // 2 MB
            FileType::Post => 5 * 1024 * 1024,          // 5 MB
            FileType::Reply => 5 * 1024 * 1024,         // 5 MB
            FileType::SectionIcon => 500 * 1024,        // 500 KB
        }
    }

    /// 验证文件大小
    pub fn validate_file_size(data: &[u8], file_type: FileType) -> Result<(), FileError> {
        let size = data.len();
        let limit = Self::get_size_limit(file_type);

        if size == 0 {
            return Err(FileError::EmptyFile);
        }

        if size > limit {
            return Err(FileError::FileTooLarge { size, max: limit });
        }

        Ok(())
    }

    /// 验证文件扩展名
    pub fn validate_extension(filename: &str) -> Result<String, FileError> {
        let path = std::path::Path::new(filename);
        let ext = path
            .extension()
            .and_then(|e| e.to_str())
            .ok_or_else(|| FileError::InvalidFileName("File has no extension".to_string()))?
            .to_lowercase();

        match ext.as_str() {
            "jpg" | "jpeg" | "png" | "gif" | "svg" => Ok(ext),
            _ => Err(FileError::UnsupportedFileType(format!(
                "Extension '{}' not supported",
                ext
            ))),
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_get_size_limit() {
        assert_eq!(FileValidator::get_size_limit(FileType::Avatar), 2_097_152);
        assert_eq!(FileValidator::get_size_limit(FileType::Post), 5_242_880);
        assert_eq!(
            FileValidator::get_size_limit(FileType::SectionIcon),
            512_000
        );
    }

    #[test]
    fn test_validate_extension() {
        assert!(FileValidator::validate_extension("test.jpg").is_ok());
        assert!(FileValidator::validate_extension("test.PNG").is_ok());
        assert!(FileValidator::validate_extension("test.pdf").is_err());
    }
}

