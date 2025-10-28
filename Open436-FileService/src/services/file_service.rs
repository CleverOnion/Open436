use anyhow::Result;
use sqlx::PgPool;
use uuid::Uuid;

use crate::models::{CreateFileRequest, File, FileStatus, FileType};
use crate::utils::error::FileError;

pub struct FileService;

impl FileService {
    /// 创建文件记录
    pub async fn create_file(pool: &PgPool, req: CreateFileRequest) -> Result<File, sqlx::Error> {
        let file = sqlx::query_as!(
            File,
            r#"
            INSERT INTO files (
                filename, storage_key, file_type, mime_type, file_size, uploader_id
            ) VALUES ($1, $2, $3, $4, $5, $6)
            RETURNING 
                id, filename, storage_key,
                file_type as "file_type: FileType",
                mime_type, file_size, uploader_id,
                status as "status: FileStatus",
                created_at, updated_at
            "#,
            req.filename,
            req.storage_key,
            req.file_type as FileType,
            req.mime_type,
            req.file_size,
            req.uploader_id
        )
        .fetch_one(pool)
        .await?;

        Ok(file)
    }

    /// 根据 ID 获取文件
    pub async fn get_file_by_id(pool: &PgPool, file_id: Uuid) -> Result<File, FileError> {
        let file = sqlx::query_as!(
            File,
            r#"
            SELECT 
                id, filename, storage_key,
                file_type as "file_type: FileType",
                mime_type, file_size, uploader_id,
                status as "status: FileStatus",
                created_at, updated_at
            FROM files
            WHERE id = $1
            "#,
            file_id
        )
        .fetch_optional(pool)
        .await?
        .ok_or_else(|| FileError::FileNotFound(file_id.to_string()))?;

        Ok(file)
    }

    /// 批量获取文件
    pub async fn get_files_by_ids(pool: &PgPool, file_ids: &[Uuid]) -> Result<Vec<File>, sqlx::Error> {
        let files = sqlx::query_as!(
            File,
            r#"
            SELECT 
                id, filename, storage_key,
                file_type as "file_type: FileType",
                mime_type, file_size, uploader_id,
                status as "status: FileStatus",
                created_at, updated_at
            FROM files
            WHERE id = ANY($1)
            "#,
            file_ids
        )
        .fetch_all(pool)
        .await?;

        Ok(files)
    }

    /// 更新文件状态
    pub async fn update_file_status(
        pool: &PgPool,
        file_id: Uuid,
        status: FileStatus,
    ) -> Result<(), sqlx::Error> {
        sqlx::query!(
            r#"
            UPDATE files
            SET status = $2, updated_at = CURRENT_TIMESTAMP
            WHERE id = $1
            "#,
            file_id,
            status as FileStatus
        )
        .execute(pool)
        .await?;

        Ok(())
    }

    /// 删除文件（标记为已删除）
    pub async fn mark_file_deleted(pool: &PgPool, file_id: Uuid) -> Result<(), sqlx::Error> {
        sqlx::query!(
            r#"
            UPDATE files
            SET status = 'deleted', updated_at = CURRENT_TIMESTAMP
            WHERE id = $1
            "#,
            file_id
        )
        .execute(pool)
        .await?;

        // 删除所有使用关联
        sqlx::query!(
            r#"
            DELETE FROM file_usages
            WHERE file_id = $1
            "#,
            file_id
        )
        .execute(pool)
        .await?;

        Ok(())
    }
}

