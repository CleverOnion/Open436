-- ========================================
-- M7 文件存储服务 - 索引创建
-- ========================================

-- files 表索引
CREATE UNIQUE INDEX idx_files_storage_key ON files(storage_key);
CREATE INDEX idx_files_uploader_id ON files(uploader_id);
CREATE INDEX idx_files_status ON files(status);
CREATE INDEX idx_files_file_type ON files(file_type);
CREATE INDEX idx_files_created_at ON files(created_at);

-- 部分索引：用于清理任务查询（仅索引未使用文件）
CREATE INDEX idx_files_status_created 
    ON files(status, created_at) 
    WHERE status = 'unused';

-- file_usages 表索引
CREATE INDEX idx_file_usages_file_id ON file_usages(file_id);
CREATE INDEX idx_file_usages_usage ON file_usages(usage_type, usage_id);

-- cleanup_logs 表索引
CREATE INDEX idx_cleanup_logs_time ON cleanup_logs(cleanup_time DESC);
CREATE INDEX idx_cleanup_logs_status ON cleanup_logs(status);
