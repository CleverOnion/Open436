-- 清理测试数据
DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users_auth WHERE username IN ('test_admin', 'test_user', 'test_disabled'));
DELETE FROM users_auth WHERE username IN ('test_admin', 'test_user', 'test_disabled');

-- 插入测试用户
-- 密码均为 test123，使用BCrypt加密（strength=10）
-- BCrypt在线工具生成的哈希

INSERT INTO users_auth (id, username, password_hash, status, last_login_at, created_at, updated_at) VALUES
(9001, 'test_admin', '$2a$10$8.H3TfhkzNBKKFvvT5dY3OQPmLhqQbWqJ0XEhH3fBaL0TfB9YOPkm', 'active', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9002, 'test_user', '$2a$10$8.H3TfhkzNBKKFvvT5dY3OQPmLhqQbWqJ0XEhH3fBaL0TfB9YOPkm', 'active', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9003, 'test_disabled', '$2a$10$8.H3TfhkzNBKKFvvT5dY3OQPmLhqQbWqJ0XEhH3fBaL0TfB9YOPkm', 'disabled', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 分配角色（假设roles表中已有id=1的admin和id=2的user角色）
INSERT INTO user_roles (user_id, role_id) VALUES
(9001, 1),  -- test_admin -> admin
(9002, 2),  -- test_user -> user
(9003, 2);  -- test_disabled -> user

-- 重置序列（如果需要）
-- SELECT setval('users_auth_id_seq', 10000, false);

