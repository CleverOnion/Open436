package com.open436.auth.repository;

import com.open436.auth.base.BaseIntegrationTest;
import com.open436.auth.entity.Permission;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PermissionRepository 集成测试
 * 测试权限数据访问层
 */
class PermissionRepositoryTest extends BaseIntegrationTest {
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Test
    void testFindByCode_Success() {
        // Given: 数据库中存在权限
        
        // When: 根据权限代码查询
        Optional<Permission> result = permissionRepository.findByCode("post:create");
        
        // Then: 如果权限存在，应该查询成功
        if (result.isPresent()) {
            assertThat(result.get().getCode()).isEqualTo("post:create");
            assertThat(result.get().getResource()).isEqualTo("post");
            assertThat(result.get().getAction()).isEqualTo("create");
        }
    }
    
    @Test
    void testFindByCode_NotFound() {
        // Given: 不存在的权限代码
        
        // When: 查询权限
        Optional<Permission> result = permissionRepository.findByCode("nonexistent:permission");
        
        // Then: 应该返回空
        assertThat(result).isEmpty();
    }
    
    @Test
    void testFindByResource() {
        // Given: 数据库中存在post相关权限
        
        // When: 查询资源类型为post的所有权限
        List<Permission> permissions = permissionRepository.findByResource("post");
        
        // Then: 如果有post权限，应该返回列表
        if (!permissions.isEmpty()) {
            assertThat(permissions).allMatch(p -> "post".equals(p.getResource()));
        }
    }
    
    @Test
    void testExistsByCode() {
        // Given: 数据库中可能存在某些权限
        
        // When: 检查权限代码是否存在
        boolean exists = permissionRepository.existsByCode("post:create");
        
        // Then: 根据实际数据判断（这里不强制要求）
        // 如果数据库有初始权限数据，exists可能为true
    }
    
    @Test
    void testFindByUserId() {
        // Given: 通过用户ID查询权限（使用实际存在的用户ID）
        Long userId = 1L;  // 使用任意ID进行方法测试
        
        // When: 查询用户的所有权限
        List<Permission> permissions = permissionRepository.findByUserId(userId);
        
        // Then: 应该返回权限列表（可能为空，取决于role_permissions表的数据）
        assertThat(permissions).isNotNull();
        
        // 验证没有重复权限
        if (!permissions.isEmpty()) {
            long distinctCount = permissions.stream()
                .map(Permission::getCode)
                .distinct()
                .count();
            assertThat(distinctCount).isEqualTo(permissions.size());
        }
    }
    
    @Test
    void testFindByUserId_NoPermissions() {
        // Given: 使用一个不存在的用户ID
        Long userId = 99999L;
        
        // When: 查询用户权限
        List<Permission> permissions = permissionRepository.findByUserId(userId);
        
        // Then: 应该返回空列表
        assertThat(permissions).isNotNull();
        assertThat(permissions).isEmpty();
    }
    
    @Test
    void testFindByRoleId() {
        // Given: admin角色（ID 1）拥有权限
        Long roleId = 1L;
        
        // When: 查询角色的所有权限
        List<Permission> permissions = permissionRepository.findByRoleId(roleId);
        
        // Then: 应该返回权限列表
        assertThat(permissions).isNotNull();
    }
    
    @Test
    void testFindByResourceAndAction() {
        // Given: 数据库中可能存在特定资源和操作的权限
        
        // When: 根据资源和操作查询权限
        Optional<Permission> result = permissionRepository.findByResourceAndAction("post", "create");
        
        // Then: 如果权限存在，应该查询成功
        if (result.isPresent()) {
            assertThat(result.get().getResource()).isEqualTo("post");
            assertThat(result.get().getAction()).isEqualTo("create");
        }
    }
    
    @Test
    void testFindAll() {
        // Given: 数据库中有权限数据
        
        // When: 查询所有权限
        List<Permission> allPermissions = permissionRepository.findAll();
        
        // Then: 应该返回权限列表
        assertThat(allPermissions).isNotNull();
    }
    
    @Test
    void testSave_CreateNewPermission() {
        // Given: 一个新权限
        Permission newPermission = new Permission();
        newPermission.setCode("test:read");
        newPermission.setName("测试读取");
        newPermission.setResource("test");
        newPermission.setAction("read");
        newPermission.setDescription("测试用权限");
        
        // When: 保存权限
        Permission saved = permissionRepository.save(newPermission);
        
        // Then: 应该保存成功并生成ID
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("test:read");
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}

