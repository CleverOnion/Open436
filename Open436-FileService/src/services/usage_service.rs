use anyhow::Result;
use sqlx::PgPool;
use uuid::Uuid;

use crate::models::FileUsage;
use crate::utils::error::FileError;

pub struct UsageService;

impl UsageService {
    /// 标记文件已使用
    pub async fn mark_used(
        pool: &PgPool,
        file_id: Uuid,
        usage_type: &str,
        usage_id: i32,
    ) -> Result<FileUsage, FileError> {
        // 检查是否已存在
        let existing = sqlx::query!(
            r#"
            SELECT id FROM file_usages
            WHERE file_id = $1 AND usage_type = $2 AND usage_id = $3
            "#,
            file_id,
            usage_type,
            usage_id
        )
        .fetch_optional(pool)
        .await?;

        if existing.is_some() {
            return Err(FileError::AlreadyMarked);
        }

        // 插入使用关联
        let usage = sqlx::query_as!(
            FileUsage,
            r#"
            INSERT INTO file_usages (file_id, usage_type, usage_id)
            VALUES ($1, $2, $3)
            RETURNING id, file_id, usage_type, usage_id, created_at
            "#,
            file_id,
            usage_type,
            usage_id
        )
        .fetch_one(pool)
        .await?;

        // 更新文件状态为 used
        sqlx::query!(
            r#"
            UPDATE files
            SET status = 'used', updated_at = CURRENT_TIMESTAMP
            WHERE id = $1
            "#,
            file_id
        )
        .execute(pool)
        .await?;

        Ok(usage)
    }

    /// 标记文件未使用
    pub async fn mark_unused(
        pool: &PgPool,
        file_id: Uuid,
        usage_type: &str,
        usage_id: i32,
    ) -> Result<i32, FileError> {
        // 删除使用关联
        let result = sqlx::query!(
            r#"
            DELETE FROM file_usages
            WHERE file_id = $1 AND usage_type = $2 AND usage_id = $3
            "#,
            file_id,
            usage_type,
            usage_id
        )
        .execute(pool)
        .await?;

        if result.rows_affected() == 0 {
            return Err(FileError::UsageNotFound);
        }

        // 检查是否还有其他使用
        let remaining_count = sqlx::query!(
            r#"
            SELECT COUNT(*) as count FROM file_usages
            WHERE file_id = $1
            "#,
            file_id
        )
        .fetch_one(pool)
        .await?
        .count
        .unwrap_or(0);

        // 如果没有其他使用，更新状态为 unused
        if remaining_count == 0 {
            sqlx::query!(
                r#"
                UPDATE files
                SET status = 'unused', updated_at = CURRENT_TIMESTAMP
                WHERE id = $1
                "#,
                file_id
            )
            .execute(pool)
            .await?;
        }

        Ok(remaining_count as i32)
    }

    /// 获取文件的所有使用位置
    pub async fn get_file_usages(pool: &PgPool, file_id: Uuid) -> Result<Vec<FileUsage>, sqlx::Error> {
        let usages = sqlx::query_as!(
            FileUsage,
            r#"
            SELECT id, file_id, usage_type, usage_id, created_at
            FROM file_usages
            WHERE file_id = $1
            "#,
            file_id
        )
        .fetch_all(pool)
        .await?;

        Ok(usages)
    }
}

