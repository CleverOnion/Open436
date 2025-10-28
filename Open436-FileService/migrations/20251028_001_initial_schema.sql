-- ========================================
-- M7 文件存储服务 - 数据库初始化
-- ========================================
-- 所有表创建在 public schema 中（与 M1 共享）
-- ========================================

-- 创建文件类型枚举
CREATE TYPE file_type AS ENUM ('avatar', 'post', 'reply', 'section_icon');

-- 创建文件状态枚举
CREATE TYPE file_status AS ENUM ('unused', 'used', 'deleted');

-- 创建文件表
CREATE TABLE files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    filename VARCHAR(255) NOT NULL,
    storage_key VARCHAR(500) NOT NULL UNIQUE,
    file_type file_type NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    uploader_id INTEGER NOT NULL,
    status file_status NOT NULL DEFAULT 'unused',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_filename_not_empty CHECK (LENGTH(filename) > 0),
    CONSTRAINT chk_storage_key_not_empty CHECK (LENGTH(storage_key) > 0),
    CONSTRAINT chk_file_size_positive CHECK (file_size > 0),
    CONSTRAINT chk_uploader_id_positive CHECK (uploader_id > 0)
);

-- 添加注释
COMMENT ON TABLE files IS 'M7 文件元数据表';
COMMENT ON COLUMN files.id IS '文件ID（UUID）';
COMMENT ON COLUMN files.filename IS '原始文件名（如：avatar.jpg）';
COMMENT ON COLUMN files.storage_key IS '存储路径/键（如：2025/10/28/uuid.jpg）';
COMMENT ON COLUMN files.file_type IS '文件类型：avatar/post/reply/section_icon';
COMMENT ON COLUMN files.mime_type IS 'MIME类型（如：image/jpeg）';
COMMENT ON COLUMN files.file_size IS '文件大小（字节）';
COMMENT ON COLUMN files.uploader_id IS '上传者ID（关联 users_auth.id）';
COMMENT ON COLUMN files.status IS '文件状态：unused/used/deleted';
COMMENT ON COLUMN files.created_at IS '上传时间';
COMMENT ON COLUMN files.updated_at IS '更新时间';

-- 创建 file_usages 表
CREATE TABLE file_usages (
    id SERIAL PRIMARY KEY,
    file_id UUID NOT NULL,
    usage_type VARCHAR(20) NOT NULL,
    usage_id INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_file FOREIGN KEY (file_id) 
        REFERENCES files(id) ON DELETE CASCADE,
    CONSTRAINT uk_file_usage UNIQUE (file_id, usage_type, usage_id),
    CONSTRAINT chk_usage_type CHECK (usage_type IN ('post', 'reply', 'avatar', 'section_icon')),
    CONSTRAINT chk_usage_id_positive CHECK (usage_id > 0)
);

COMMENT ON TABLE file_usages IS 'M7 文件使用关联表';
COMMENT ON COLUMN file_usages.id IS '关联ID（自增主键）';
COMMENT ON COLUMN file_usages.file_id IS '文件ID（外键）';
COMMENT ON COLUMN file_usages.usage_type IS '使用类型：post/reply/avatar/section_icon';
COMMENT ON COLUMN file_usages.usage_id IS '使用位置ID（帖子ID/回复ID/用户ID/板块ID）';
COMMENT ON COLUMN file_usages.created_at IS '关联创建时间';

-- 创建清理日志表
CREATE TABLE cleanup_logs (
    id SERIAL PRIMARY KEY,
    cleanup_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    files_deleted INTEGER NOT NULL DEFAULT 0,
    space_freed BIGINT NOT NULL DEFAULT 0,
    duration_ms INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'success',
    error_message TEXT,
    
    CONSTRAINT chk_files_deleted_positive CHECK (files_deleted >= 0),
    CONSTRAINT chk_space_freed_positive CHECK (space_freed >= 0),
    CONSTRAINT chk_status CHECK (status IN ('success', 'partial', 'failed'))
);

COMMENT ON TABLE cleanup_logs IS 'M7 自动清理日志表';
COMMENT ON COLUMN cleanup_logs.id IS '日志ID（自增主键）';
COMMENT ON COLUMN cleanup_logs.cleanup_time IS '清理时间';
COMMENT ON COLUMN cleanup_logs.files_deleted IS '删除文件数';
COMMENT ON COLUMN cleanup_logs.space_freed IS '释放空间（字节）';
COMMENT ON COLUMN cleanup_logs.duration_ms IS '执行时长（毫秒）';
COMMENT ON COLUMN cleanup_logs.status IS '执行状态：success/partial/failed';
COMMENT ON COLUMN cleanup_logs.error_message IS '错误信息（失败时）';
