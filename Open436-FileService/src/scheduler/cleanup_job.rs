use sqlx::PgPool;
use std::sync::Arc;
use tokio_cron_scheduler::{Job, JobScheduler};
use tracing::{error, info};

use crate::config::CleanupConfig;
use crate::services::CleanupService;
use crate::storage::StorageBackend;

/// 启动清理定时任务
pub async fn start_cleanup_job(
    pool: Arc<PgPool>,
    storage: Arc<dyn StorageBackend>,
    config: &CleanupConfig,
) -> anyhow::Result<()> {
    if !config.enabled {
        info!("Cleanup job is disabled");
        return Ok(());
    }

    let scheduler = JobScheduler::new().await?;

    // 创建定时任务
    let pool_clone = pool.clone();
    let storage_clone = storage.clone();
    let threshold_days = config.days_threshold;
    let batch_size = config.batch_size;
    let cron_expr = config.cron_expression.clone();

    let job = Job::new_async(cron_expr.as_str(), move |_uuid, _l| {
        let pool = pool_clone.clone();
        let storage = storage_clone.clone();

        Box::pin(async move {
            info!("Starting scheduled cleanup job");

            match CleanupService::cleanup_unused_files(
                &pool,
                &storage,
                threshold_days,
                batch_size,
                false, // dry_run = false
            )
            .await
            {
                Ok(result) => {
                    info!(
                        files_deleted = result.files_deleted,
                        space_freed = result.space_freed,
                        duration_ms = result.duration_ms,
                        "Cleanup job completed successfully"
                    );
                }
                Err(e) => {
                    error!(error = %e, "Cleanup job failed");
                }
            }
        })
    })?;

    scheduler.add(job).await?;
    scheduler.start().await?;

    info!(cron = cron_expr, "Cleanup job scheduled");

    Ok(())
}

