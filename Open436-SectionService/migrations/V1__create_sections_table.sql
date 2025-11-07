-- =====================================================
-- M5 板块管理服务 - 数据库迁移脚本
-- 版本: V1
-- 创建日期: 2025-11-04
-- 说明: 创建 sections 表及相关索引
-- =====================================================

-- 1. 创建 sections 表
CREATE TABLE IF NOT EXISTS public.sections (
    id SERIAL PRIMARY KEY,
    slug VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    icon_file_id UUID,
    color VARCHAR(7) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 100,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    posts_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_sort_order CHECK (sort_order >= 1 AND sort_order <= 999),
    CONSTRAINT chk_posts_count CHECK (posts_count >= 0),
    CONSTRAINT chk_color_format CHECK (color ~ '^#[0-9A-Fa-f]{6}$'),
    CONSTRAINT chk_slug_format CHECK (slug ~ '^[a-z0-9_]+$'),
    CONSTRAINT fk_icon_file FOREIGN KEY (icon_file_id) 
        REFERENCES public.files(id) ON DELETE SET NULL
);

-- 2. 创建索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_sections_slug 
    ON public.sections(slug);

CREATE UNIQUE INDEX IF NOT EXISTS idx_sections_name 
    ON public.sections(name);

CREATE INDEX IF NOT EXISTS idx_sections_sort_order 
    ON public.sections(sort_order, id);

CREATE INDEX IF NOT EXISTS idx_sections_is_enabled 
    ON public.sections(is_enabled);

CREATE INDEX IF NOT EXISTS idx_sections_enabled_sort 
    ON public.sections(is_enabled, sort_order, id);

-- 3. 创建更新时间触发器函数（如果不存在）
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 4. 创建触发器（自动更新 updated_at）
DROP TRIGGER IF EXISTS update_sections_updated_at ON public.sections;
CREATE TRIGGER update_sections_updated_at
    BEFORE UPDATE ON public.sections
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 5. 插入初始数据（6 个预设板块）
INSERT INTO public.sections (slug, name, description, color, sort_order, is_enabled, posts_count) 
VALUES
    ('tech', '技术交流', '分享编程技术和开发经验，讨论最新技术趋势', '#1976D2', 1, TRUE, 0),
    ('design', '设计分享', 'UI/UX 设计作品展示、设计心得分享', '#9C27B0', 2, TRUE, 0),
    ('discuss', '综合讨论', '各类话题的自由讨论空间', '#4CAF50', 3, TRUE, 0),
    ('question', '问答求助', '技术问题求助、疑难解答互助', '#FF9800', 4, TRUE, 0),
    ('share', '资源分享', '开发工具、学习教程、开源项目推荐', '#00BCD4', 5, TRUE, 0),
    ('announce', '公告通知', '官方公告、重要通知、系统更新', '#F44336', 6, TRUE, 0)
ON CONFLICT (slug) DO NOTHING;

-- 6. 添加表注释
COMMENT ON TABLE public.sections IS '板块表 - 论坛板块信息';
COMMENT ON COLUMN public.sections.id IS '板块ID（主键）';
COMMENT ON COLUMN public.sections.slug IS '板块标识（用于URL，唯一）';
COMMENT ON COLUMN public.sections.name IS '板块名称（唯一）';
COMMENT ON COLUMN public.sections.description IS '板块描述（支持Markdown）';
COMMENT ON COLUMN public.sections.icon_file_id IS '板块图标文件ID（外键 → files.id）';
COMMENT ON COLUMN public.sections.color IS '板块颜色（HEX格式）';
COMMENT ON COLUMN public.sections.sort_order IS '排序号（1-999，越小越靠前）';
COMMENT ON COLUMN public.sections.is_enabled IS '启用状态（FALSE为禁用）';
COMMENT ON COLUMN public.sections.posts_count IS '帖子数量（冗余字段）';
COMMENT ON COLUMN public.sections.created_at IS '创建时间';
COMMENT ON COLUMN public.sections.updated_at IS '最后更新时间';

-- 7. 验证数据
SELECT 
    id,
    slug,
    name,
    color,
    sort_order,
    is_enabled,
    posts_count
FROM public.sections
ORDER BY sort_order;




