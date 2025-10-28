use serde::{Deserialize, Serialize};
use std::fmt;
use std::str::FromStr;

/// 文件类型枚举
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize, sqlx::Type)]
#[sqlx(type_name = "file_type", rename_all = "lowercase")]
#[serde(rename_all = "lowercase")]
pub enum FileType {
    Avatar,
    Post,
    Reply,
    #[serde(rename = "section_icon")]
    #[sqlx(rename = "section_icon")]
    SectionIcon,
}

impl FromStr for FileType {
    type Err = String;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s.to_lowercase().as_str() {
            "avatar" => Ok(FileType::Avatar),
            "post" => Ok(FileType::Post),
            "reply" => Ok(FileType::Reply),
            "section_icon" | "section-icon" => Ok(FileType::SectionIcon),
            _ => Err(format!("Invalid file type: {}", s)),
        }
    }
}

impl fmt::Display for FileType {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            FileType::Avatar => write!(f, "avatar"),
            FileType::Post => write!(f, "post"),
            FileType::Reply => write!(f, "reply"),
            FileType::SectionIcon => write!(f, "section_icon"),
        }
    }
}

/// 文件状态枚举
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize, sqlx::Type)]
#[sqlx(type_name = "file_status", rename_all = "lowercase")]
#[serde(rename_all = "lowercase")]
pub enum FileStatus {
    Unused,
    Used,
    Deleted,
}

impl fmt::Display for FileStatus {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            FileStatus::Unused => write!(f, "unused"),
            FileStatus::Used => write!(f, "used"),
            FileStatus::Deleted => write!(f, "deleted"),
        }
    }
}

impl FromStr for FileStatus {
    type Err = String;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s.to_lowercase().as_str() {
            "unused" => Ok(FileStatus::Unused),
            "used" => Ok(FileStatus::Used),
            "deleted" => Ok(FileStatus::Deleted),
            _ => Err(format!("Invalid file status: {}", s)),
        }
    }
}

