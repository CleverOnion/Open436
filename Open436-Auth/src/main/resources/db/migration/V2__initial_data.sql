-- M1 认证授权服务 - 初始化数据脚本
-- 版本: v1.0
-- 说明: 只包含M1认证授权模块的权限

-- ============================================
-- 1. 插入角色数据
-- ============================================
INSERT INTO roles (name, code, description) VALUES
('普通用户', 'user', '普通用户，可以查看基本信息'),
('管理员', 'admin', '拥有全站管理权限');

-- ============================================
-- 2. 插入权限数据（只包含M1模块权限）
-- ============================================

-- 用户管理权限
INSERT INTO permissions (name, code, resource, action, description) VALUES
('查看用户', 'user:read', 'user', 'read', '查看用户信息'),
('创建用户', 'user:create', 'user', 'create', '创建新用户账号'),
('编辑用户', 'user:update', 'user', 'update', '编辑用户信息'),
('删除用户', 'user:delete', 'user', 'delete', '删除用户账号'),
('管理用户', 'user:manage', 'user', 'manage', '完整的用户管理权限（包含CRUD）');

-- 角色管理权限
INSERT INTO permissions (name, code, resource, action, description) VALUES
('查看角色', 'role:read', 'role', 'read', '查看角色信息'),
('管理角色', 'role:manage', 'role', 'manage', '管理角色和权限分配');

-- 系统管理权限
INSERT INTO permissions (name, code, resource, action, description) VALUES
('系统配置', 'system:manage', 'system', 'manage', '修改系统配置');

-- 注意：帖子、回复、互动、板块等权限属于其他模块（M3、M4、M5），待相关模块开发时添加

-- ============================================
-- 3. 分配角色权限
-- ============================================

-- 普通用户权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT id FROM roles WHERE code = 'user'),
    id
FROM permissions
WHERE code IN (
    'user:read'  -- 普通用户只能查看用户信息
);

-- 管理员权限（拥有所有权限）
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT id FROM roles WHERE code = 'admin'),
    id
FROM permissions;

-- ============================================
-- 4. 创建默认管理员账号
-- ============================================

-- 密码: admin123
-- BCrypt 加密后的哈希值（cost=10）
INSERT INTO users_auth (username, password_hash, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHMnjtdFcdQwPcn2WBhZKi', 'active');

-- 分配管理员角色
INSERT INTO user_roles (user_id, role_id)
SELECT 
    (SELECT id FROM users_auth WHERE username = 'admin'),
    (SELECT id FROM roles WHERE code = 'admin');

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
