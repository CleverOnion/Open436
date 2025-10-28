use anyhow::{Context, Result};
use async_trait::async_trait;
use s3::creds::Credentials;
use s3::{Bucket, Region};

use super::backend::StorageBackend;
use crate::config::S3Config;

/// S3 存储后端（支持 Minio 和其他 S3 兼容服务）
pub struct S3StorageBackend {
    bucket: Bucket,
    public_url: String,
}

impl S3StorageBackend {
    /// 创建 S3 存储后端
    pub async fn new(config: &S3Config) -> Result<Self> {
        // 配置区域（Minio 使用自定义端点）
        let region = if let Some(endpoint) = &config.endpoint {
            Region::Custom {
                region: config.region.clone(),
                endpoint: endpoint.clone(),
            }
        } else {
            // 标准 AWS 区域
            config.region.parse().unwrap_or(Region::UsEast1)
        };

        // 配置凭证
        let credentials = Credentials::new(
            Some(&config.access_key),
            Some(&config.secret_key),
            None,
            None,
            None,
        )
        .context("Failed to create S3 credentials")?;

        // 创建 Bucket
        let mut bucket = Bucket::new(&config.bucket, region, credentials)
            .context("Failed to create S3 bucket")?;

        // Minio 需要使用 path-style URL
        if config.path_style {
            bucket = bucket.with_path_style();
        }

        Ok(Self {
            bucket,
            public_url: config.public_url.clone(),
        })
    }
}

#[async_trait]
impl StorageBackend for S3StorageBackend {
    async fn upload(&self, key: &str, data: Vec<u8>, content_type: &str) -> Result<String> {
        // 上传文件到 S3
        self.bucket
            .put_object_with_content_type(key, &data, content_type)
            .await
            .context(format!("Failed to upload file to S3: {}", key))?;

        // 返回公开访问 URL
        let url = format!("{}/{}", self.public_url, key);
        Ok(url)
    }

    async fn download(&self, key: &str) -> Result<Vec<u8>> {
        // 从 S3 下载文件
        let response = self
            .bucket
            .get_object(key)
            .await
            .context(format!("Failed to download file from S3: {}", key))?;

        Ok(response.bytes().to_vec())
    }

    async fn delete(&self, key: &str) -> Result<()> {
        // 从 S3 删除文件
        self.bucket
            .delete_object(key)
            .await
            .context(format!("Failed to delete file from S3: {}", key))?;

        Ok(())
    }

    async fn get_url(&self, key: &str) -> Result<String> {
        // 返回公开访问 URL
        let url = format!("{}/{}", self.public_url, key);
        Ok(url)
    }

    async fn exists(&self, key: &str) -> Result<bool> {
        // 检查文件是否存在
        match self.bucket.head_object(key).await {
            Ok(_) => Ok(true),
            Err(e) => {
                let error_str = e.to_string();
                // 404 表示文件不存在
                if error_str.contains("404") || error_str.contains("Not Found") {
                    Ok(false)
                } else {
                    Err(e.into())
                }
            }
        }
    }
}
