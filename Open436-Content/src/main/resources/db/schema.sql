-- =============================================
-- Open436 内容管理模块数据库表结构
-- 数据库: PostgreSQL
-- 创建日期: 2025-11-03
-- =============================================

-- 1. 帖子主表
CREATE TABLE IF NOT EXISTS post (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    board_id BIGINT NOT NULL,
    
    -- 状态信息
    is_deleted BOOLEAN DEFAULT FALSE,
    delete_reason VARCHAR(500),
    deleted_by BIGINT,
    deleted_at TIMESTAMP,
    
    pin_type SMALLINT DEFAULT 0,
    pinned_at TIMESTAMP,
    pinned_by BIGINT,
    
    -- 统计信息
    view_count BIGINT DEFAULT 0,
    reply_count INTEGER DEFAULT 0,
    like_count INTEGER DEFAULT 0,
    
    -- 编辑信息
    edit_count INTEGER DEFAULT 0,
    last_edited_at TIMESTAMP,
    last_edited_by BIGINT,
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 索引
    CONSTRAINT chk_title_length CHECK (char_length(title) >= 5 AND char_length(title) <= 100),
    CONSTRAINT chk_content_length CHECK (char_length(content) >= 10 AND char_length(content) <= 50000),
    CONSTRAINT chk_pin_type CHECK (pin_type IN (0, 1, 2))
);

-- 创建索引
CREATE INDEX idx_post_author_id ON post(author_id);
CREATE INDEX idx_post_board_id ON post(board_id);
CREATE INDEX idx_post_created_at ON post(created_at DESC);
CREATE INDEX idx_post_is_deleted ON post(is_deleted);
CREATE INDEX idx_post_pin_type ON post(pin_type);
CREATE INDEX idx_post_board_created ON post(board_id, created_at DESC);

-- 添加表注释
COMMENT ON TABLE post IS '帖子主表，存储论坛帖子的基本信息';
COMMENT ON COLUMN post.id IS '帖子ID，主键';
COMMENT ON COLUMN post.title IS '帖子标题，5-100个字符';
COMMENT ON COLUMN post.content IS '帖子内容，支持富文本，最大50000字符';
COMMENT ON COLUMN post.author_id IS '作者用户ID';
COMMENT ON COLUMN post.board_id IS '所属板块ID';
COMMENT ON COLUMN post.is_deleted IS '是否已删除（软删除）';
COMMENT ON COLUMN post.pin_type IS '置顶类型：0-不置顶，1-板块置顶，2-全局置顶';
COMMENT ON COLUMN post.view_count IS '浏览量';
COMMENT ON COLUMN post.reply_count IS '回复数';
COMMENT ON COLUMN post.like_count IS '点赞数';

-- =============================================

-- 2. 帖子编辑历史表
CREATE TABLE IF NOT EXISTS post_edit_history (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    version INTEGER NOT NULL,
    
    -- 历史内容
    old_title VARCHAR(100) NOT NULL,
    old_content TEXT NOT NULL,
    old_board_id BIGINT,
    
    -- 编辑信息
    edited_by BIGINT NOT NULL,
    edited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    edit_reason VARCHAR(500),
    
    CONSTRAINT fk_post_edit_history_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_post_edit_history_post_id ON post_edit_history(post_id);
CREATE INDEX idx_post_edit_history_edited_at ON post_edit_history(edited_at DESC);

-- 添加表注释
COMMENT ON TABLE post_edit_history IS '帖子编辑历史表，记录帖子的每次编辑';
COMMENT ON COLUMN post_edit_history.post_id IS '关联的帖子ID';
COMMENT ON COLUMN post_edit_history.version IS '版本号';
COMMENT ON COLUMN post_edit_history.old_title IS '修改前的标题';
COMMENT ON COLUMN post_edit_history.old_content IS '修改前的内容';
COMMENT ON COLUMN post_edit_history.edited_by IS '编辑者用户ID';

-- =============================================

-- 3. 帖子浏览记录表
CREATE TABLE IF NOT EXISTS post_view_record (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT,
    ip_address VARCHAR(45),
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_post_view_record_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_post_view_record_post_id ON post_view_record(post_id);
CREATE INDEX idx_post_view_record_user_id ON post_view_record(user_id);
CREATE INDEX idx_post_view_record_viewed_at ON post_view_record(viewed_at DESC);
CREATE INDEX idx_post_view_record_post_user_time ON post_view_record(post_id, user_id, viewed_at);
CREATE INDEX idx_post_view_record_post_ip_time ON post_view_record(post_id, ip_address, viewed_at);

-- 添加表注释
COMMENT ON TABLE post_view_record IS '帖子浏览记录表，用于去重统计浏览量';
COMMENT ON COLUMN post_view_record.post_id IS '被浏览的帖子ID';
COMMENT ON COLUMN post_view_record.user_id IS '浏览用户ID（登录用户）';
COMMENT ON COLUMN post_view_record.ip_address IS 'IP地址（未登录用户）';
COMMENT ON COLUMN post_view_record.viewed_at IS '浏览时间';

-- =============================================
-- 数据库表创建完成
-- =============================================

