-- M1 认证授权服务 - 数据库初始化脚本
-- 版本: v1.0
-- 数据库: PostgreSQL 14+

-- ============================================
-- 1. 创建用户认证表
-- ============================================
CREATE TABLE users_auth (
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

-- 添加注释
COMMENT ON TABLE users_auth IS '用户认证表';
COMMENT ON COLUMN users_auth.id IS '用户ID（主键）';
COMMENT ON COLUMN users_auth.username IS '用户名（唯一，3-20字符）';
COMMENT ON COLUMN users_auth.password_hash IS '密码哈希（BCrypt加密）';
COMMENT ON COLUMN users_auth.status IS '账号状态：active-正常, disabled-禁用';
COMMENT ON COLUMN users_auth.last_login_at IS '最后登录时间';
COMMENT ON COLUMN users_auth.created_at IS '创建时间';
COMMENT ON COLUMN users_auth.updated_at IS '更新时间';

-- ============================================
-- 2. 创建角色表
-- ============================================
CREATE TABLE roles (
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
COMMENT ON COLUMN roles.created_at IS '创建时间';
COMMENT ON COLUMN roles.updated_at IS '更新时间';

-- ============================================
-- 3. 创建权限表
-- ============================================
CREATE TABLE permissions (
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
COMMENT ON COLUMN permissions.description IS '权限描述';
COMMENT ON COLUMN permissions.created_at IS '创建时间';

-- ============================================
-- 4. 创建用户角色关联表
-- ============================================
CREATE TABLE user_roles (
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
COMMENT ON COLUMN user_roles.id IS '关联ID';
COMMENT ON COLUMN user_roles.user_id IS '用户ID（外键）';
COMMENT ON COLUMN user_roles.role_id IS '角色ID（外键）';
COMMENT ON COLUMN user_roles.created_at IS '创建时间';

-- ============================================
-- 5. 创建角色权限关联表
-- ============================================
CREATE TABLE role_permissions (
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL,
    permission_id INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_role FOREIGN KEY (role_id) 
        REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_permission FOREIGN KEY (permission_id) 
        REFERENCES permissions(id) ON DELETE CASCADE,
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
);

COMMENT ON TABLE role_permissions IS '角色权限关联表';
COMMENT ON COLUMN role_permissions.id IS '关联ID';
COMMENT ON COLUMN role_permissions.role_id IS '角色ID（外键）';
COMMENT ON COLUMN role_permissions.permission_id IS '权限ID（外键）';
COMMENT ON COLUMN role_permissions.created_at IS '创建时间';

-- ============================================
-- 6. 创建索引
-- ============================================

-- users_auth 表索引
CREATE UNIQUE INDEX idx_users_auth_username ON users_auth(username);
CREATE INDEX idx_users_auth_status ON users_auth(status);
CREATE INDEX idx_users_auth_created_at ON users_auth(created_at);

-- roles 表索引
CREATE UNIQUE INDEX idx_roles_name ON roles(name);
CREATE UNIQUE INDEX idx_roles_code ON roles(code);

-- permissions 表索引
CREATE UNIQUE INDEX idx_permissions_code ON permissions(code);
CREATE INDEX idx_permissions_resource ON permissions(resource);

-- user_roles 表索引
CREATE UNIQUE INDEX idx_user_roles_unique ON user_roles(user_id, role_id);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- role_permissions 表索引
CREATE UNIQUE INDEX idx_role_permissions_unique ON role_permissions(role_id, permission_id);
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- ============================================
-- 完成
-- ============================================
COMMENT ON DATABASE current_database() IS 'Open436 认证授权服务数据库';

