use std::sync::Arc;

use crate::config::S3Config;

use super::backend::StorageBackend;
use super::s3::S3StorageBackend;

/// 创建存储后端实例
/// 当前仅支持 S3（Minio），但保留抽象层便于未来扩展
pub async fn create_storage(config: &S3Config) -> Arc<dyn StorageBackend> {
    Arc::new(
        S3StorageBackend::new(config)
            .await
            .expect("Failed to create S3 storage backend"),
    )
}
