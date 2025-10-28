-- Open436 认证授权服务 - 快速初始化脚本
-- 此脚本用于快速创建数据库，包含建表和初始化数据

-- ============================================
-- 创建数据库（如果需要）
-- ============================================
-- CREATE DATABASE open436;
-- 注意：请在执行前先连接到 open436 数据库

-- ============================================
-- 1. 创建用户认证表
-- ============================================
CREATE TABLE IF NOT EXISTS users_auth (
    id SERIAL PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 3),
    CONSTRAINT chk_status CHECK (status IN ('active', 'disabled'))
);

COMMENT ON TABLE users_auth IS '用户认证表';
COMMENT ON COLUMN users_auth.id IS '用户ID（主键）';
COMMENT ON COLUMN users_auth.username IS '用户名（唯一，3-20字符）';
COMMENT ON COLUMN users_auth.password_hash IS '密码哈希（BCrypt加密）';
COMMENT ON COLUMN users_auth.status IS '账号状态：active-正常, disabled-禁用';
COMMENT ON COLUMN users_auth.last_login_at IS '最后登录时间';

-- ============================================
-- 2. 创建角色表
-- ============================================
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_code CHECK (code ~ '^[a-z_]+$')
);

COMMENT ON TABLE roles IS '角色表';
COMMENT ON COLUMN roles.id IS '角色ID（主键）';
COMMENT ON COLUMN roles.name IS '角色名称（中文）';
COMMENT ON COLUMN roles.code IS '角色代码（英文，小写+下划线）';
COMMENT ON COLUMN roles.description IS '角色描述';

-- ============================================
-- 3. 创建权限表
-- ============================================
CREATE TABLE IF NOT EXISTS permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_action CHECK (action IN ('create', 'read', 'update', 'delete', 'manage'))
);

COMMENT ON TABLE permissions IS '权限表';
COMMENT ON COLUMN permissions.id IS '权限ID（主键）';
COMMENT ON COLUMN permissions.name IS '权限名称（中文）';
COMMENT ON COLUMN permissions.code IS '权限代码（唯一标识）';
COMMENT ON COLUMN permissions.resource IS '资源类型（如：post, user, section）';
COMMENT ON COLUMN permissions.action IS '操作类型（create, read, update, delete, manage）';

-- ============================================
-- 4. 创建用户角色关联表
-- ============================================
CREATE TABLE IF NOT EXISTS user_roles (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user FOREIGN KEY (user_id) 
        REFERENCES users_auth(id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) 
        REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

COMMENT ON TABLE user_roles IS '用户角色关联表';
COMMENT ON COLUMN user_roles.user_id IS '用户ID（外键）';
COMMENT ON COLUMN user_roles.role_id IS '角色ID（外键）';

-- ============================================
-- 5. 创建角色权限关联表
-- ============================================
CREATE TABLE IF NOT EXISTS role_permissions (
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL,
    permission_id INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_role_rp FOREIGN KEY (role_id) 
        REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_permission FOREIGN KEY (permission_id) 
        REFERENCES permissions(id) ON DELETE CASCADE,
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
);

COMMENT ON TABLE role_permissions IS '角色权限关联表';
COMMENT ON COLUMN role_permissions.role_id IS '角色ID（外键）';
COMMENT ON COLUMN role_permissions.permission_id IS '权限ID（外键）';

-- ============================================
-- 6. 创建索引
-- ============================================

-- users_auth 表索引
CREATE INDEX IF NOT EXISTS idx_users_auth_status ON users_auth(status);
CREATE INDEX IF NOT EXISTS idx_users_auth_created_at ON users_auth(created_at);

-- permissions 表索引
CREATE INDEX IF NOT EXISTS idx_permissions_resource ON permissions(resource);

-- user_roles 表索引
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

-- role_permissions 表索引
CREATE INDEX IF NOT EXISTS idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_id ON role_permissions(permission_id);

-- ============================================
-- 7. 插入初始化数据
-- ============================================

-- 插入角色
INSERT INTO roles (name, code, description) VALUES
('普通用户', 'user', '可以发帖、回复、点赞、收藏'),
('管理员', 'admin', '拥有全站管理权限')
ON CONFLICT (code) DO NOTHING;

-- 插入权限（只包含M1认证授权模块的权限）
INSERT INTO permissions (name, code, resource, action, description) VALUES
-- 用户管理权限
('查看用户', 'user:read', 'user', 'read', '查看用户信息'),
('创建用户', 'user:create', 'user', 'create', '创建新用户账号'),
('编辑用户', 'user:update', 'user', 'update', '编辑用户信息'),
('删除用户', 'user:delete', 'user', 'delete', '删除用户账号'),
('管理用户', 'user:manage', 'user', 'manage', '完整的用户管理权限（包含CRUD）'),
-- 角色管理权限
('查看角色', 'role:read', 'role', 'read', '查看角色信息'),
('管理角色', 'role:manage', 'role', 'manage', '管理角色和权限分配'),
-- 系统管理权限
('系统配置', 'system:manage', 'system', 'manage', '修改系统配置')
ON CONFLICT (code) DO NOTHING;

-- 注意：帖子、回复、互动、板块等权限属于其他模块，待相关模块开发时添加

-- ============================================
-- 8. 分配角色权限
-- ============================================

-- 普通用户权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT id FROM roles WHERE code = 'user'),
    id
FROM permissions
WHERE code IN (
    'user:read'  -- 普通用户只能查看用户信息
)
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 管理员权限（拥有所有权限）
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT id FROM roles WHERE code = 'admin'),
    id
FROM permissions
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================
-- 9. 创建默认管理员账号
-- ============================================

-- 密码: admin123
-- BCrypt 加密后的哈希值（cost=10）
-- 注意：这是示例密码，生产环境请修改
INSERT INTO users_auth (username, password_hash, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHMnjtdFcdQwPcn2WBhZKi', 'active')
ON CONFLICT (username) DO NOTHING;

-- 分配管理员角色
INSERT INTO user_roles (user_id, role_id)
SELECT 
    (SELECT id FROM users_auth WHERE username = 'admin'),
    (SELECT id FROM roles WHERE code = 'admin')
ON CONFLICT (user_id, role_id) DO NOTHING;

-- ============================================
-- 完成
-- ============================================
-- 数据初始化完成
-- 角色数: 2（user, admin）
-- 权限数: 8（M1模块权限）
-- 管理员账号: admin / admin123
-- 
-- 说明：
-- - 普通用户权限: user:read（查看用户信息）
-- - 管理员权限: 所有8个权限
-- - 其他模块（M3帖子、M4评论、M5板块等）的权限待后续添加

