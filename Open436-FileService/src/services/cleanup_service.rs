use anyhow::Result;
use chrono::Utc;
use sqlx::PgPool;
use std::sync::Arc;
use tracing::{debug, error, info};
use uuid::Uuid;

use crate::models::CleanupResult;
use crate::storage::StorageBackend;
use crate::utils::path_generator::format_size;

#[derive(Debug)]
struct FileToCleanup {
    id: Uuid,
    storage_key: String,
    file_size: i64,
}

pub struct CleanupService;

impl CleanupService {
    /// 清理未使用文件
    pub async fn cleanup_unused_files(
        pool: &PgPool,
        storage: &Arc<dyn StorageBackend>,
        days_threshold: i32,
        batch_size: usize,
        dry_run: bool,
    ) -> Result<CleanupResult> {
        let start_time = Utc::now();
        let mut files_deleted = 0;
        let mut space_freed: i64 = 0;
        let mut errors = Vec::new();

        loop {
            // 1. 查询待清理文件（批量）
            let files = Self::fetch_files_to_cleanup(pool, days_threshold, batch_size).await?;

            if files.is_empty() {
                break;
            }

            info!(batch_size = files.len(), "Found files to cleanup");

            // 2. 处理每个文件
            for file in files.iter() {
                if dry_run {
                    // 仅列出，不删除
                    info!(
                        file_id = %file.id,
                        storage_key = file.storage_key,
                        size = file.file_size,
                        "Would delete file (dry run)"
                    );
                    files_deleted += 1;
                    space_freed += file.file_size;
                    continue;
                }

                // 3. 从存储后端删除物理文件
                match storage.delete(&file.storage_key).await {
                    Ok(_) => {
                        debug!(
                            file_id = %file.id,
                            storage_key = file.storage_key,
                            "File deleted from storage"
                        );

                        // 4. 更新数据库状态
                        match Self::mark_file_deleted(pool, &file.id).await {
                            Ok(_) => {
                                files_deleted += 1;
                                space_freed += file.file_size;
                            }
                            Err(e) => {
                                let error_msg =
                                    format!("Failed to mark file {} as deleted: {}", file.id, e);
                                error!(error_msg);
                                errors.push(error_msg);
                            }
                        }
                    }
                    Err(e) => {
                        let error_msg =
                            format!("Failed to delete file {} from storage: {}", file.id, e);
                        error!(error_msg);
                        errors.push(error_msg);
                    }
                }
            }

            // 批量处理，避免一次处理太多
            if files.len() < batch_size {
                break;
            }
        }

        let duration = Utc::now().signed_duration_since(start_time);
        let duration_ms = duration.num_milliseconds();

        let status = if errors.is_empty() {
            "success".to_string()
        } else if files_deleted > 0 {
            "partial".to_string()
        } else {
            "failed".to_string()
        };

        let result = CleanupResult {
            files_deleted,
            space_freed,
            space_freed_pretty: format_size(space_freed),
            duration_ms,
            status: status.clone(),
            errors: errors.clone(),
        };

        // 5. 记录清理日志
        if !dry_run {
            Self::log_cleanup(pool, &result).await?;
        }

        Ok(result)
    }

    /// 查询待清理文件
    async fn fetch_files_to_cleanup(
        pool: &PgPool,
        days_threshold: i32,
        limit: usize,
    ) -> Result<Vec<FileToCleanup>> {
        let files = sqlx::query_as!(
            FileToCleanup,
            r#"
            SELECT id, storage_key, file_size
            FROM files
            WHERE status = 'unused'
              AND created_at < CURRENT_TIMESTAMP - make_interval(days => $1)
            ORDER BY created_at
            LIMIT $2
            "#,
            days_threshold,
            limit as i64
        )
        .fetch_all(pool)
        .await?;

        Ok(files)
    }

    /// 标记文件为已删除
    async fn mark_file_deleted(pool: &PgPool, file_id: &Uuid) -> Result<()> {
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

        // 删除使用关联（如果有）
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

    /// 记录清理日志
    async fn log_cleanup(pool: &PgPool, result: &CleanupResult) -> Result<()> {
        let error_message = if result.errors.is_empty() {
            None
        } else {
            Some(result.errors.join("; "))
        };

        sqlx::query!(
            r#"
            INSERT INTO cleanup_logs (
                cleanup_time,
                files_deleted,
                space_freed,
                duration_ms,
                status,
                error_message
            ) VALUES (CURRENT_TIMESTAMP, $1, $2, $3, $4, $5)
            "#,
            result.files_deleted,
            result.space_freed,
            result.duration_ms as i32,
            result.status,
            error_message
        )
        .execute(pool)
        .await?;

        Ok(())
    }
}

