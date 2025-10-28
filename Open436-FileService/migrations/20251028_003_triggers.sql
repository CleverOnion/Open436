-- ========================================
-- M7 文件存储服务 - 触发器创建
-- ========================================

-- 自动更新 updated_at 触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 应用到 files 表
CREATE TRIGGER trg_update_files_updated_at
    BEFORE UPDATE ON files
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 状态转换检查触发器函数
CREATE OR REPLACE FUNCTION check_status_transition()
RETURNS TRIGGER AS $$
BEGIN
    -- 已删除的文件不能更改状态
    IF OLD.status = 'deleted' AND NEW.status != 'deleted' THEN
        RAISE EXCEPTION 'Cannot change status of deleted file';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 应用到 files 表
CREATE TRIGGER trg_check_status_transition
    BEFORE UPDATE ON files
    FOR EACH ROW
    EXECUTE FUNCTION check_status_transition();
