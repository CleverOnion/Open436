use async_trait::async_trait;
use anyhow::Result;

/// 存储后端抽象接口
#[async_trait]
pub trait StorageBackend: Send + Sync {
    /// 上传文件
    ///
    /// # 参数
    /// - `key`: 存储路径/键（如：2025/10/28/uuid.jpg）
    /// - `data`: 文件二进制数据
    /// - `content_type`: MIME 类型（如：image/jpeg）
    ///
    /// # 返回
    /// - `Ok(url)`: 上传成功，返回访问 URL
    /// - `Err(e)`: 上传失败
    async fn upload(&self, key: &str, data: Vec<u8>, content_type: &str) -> Result<String>;

    /// 下载文件
    ///
    /// # 参数
    /// - `key`: 存储路径/键
    ///
    /// # 返回
    /// - `Ok(data)`: 下载成功，返回文件数据
    /// - `Err(e)`: 下载失败（如文件不存在）
    async fn download(&self, key: &str) -> Result<Vec<u8>>;

    /// 删除文件
    ///
    /// # 参数
    /// - `key`: 存储路径/键
    ///
    /// # 返回
    /// - `Ok(())`: 删除成功
    /// - `Err(e)`: 删除失败
    async fn delete(&self, key: &str) -> Result<()>;

    /// 获取文件访问 URL
    ///
    /// # 参数
    /// - `key`: 存储路径/键
    ///
    /// # 返回
    /// - `Ok(url)`: 返回可访问的 URL
    /// - `Err(e)`: 获取失败
    async fn get_url(&self, key: &str) -> Result<String>;

    /// 检查文件是否存在
    ///
    /// # 参数
    /// - `key`: 存储路径/键
    ///
    /// # 返回
    /// - `Ok(true)`: 文件存在
    /// - `Ok(false)`: 文件不存在
    /// - `Err(e)`: 检查失败
    async fn exists(&self, key: &str) -> Result<bool>;
}

