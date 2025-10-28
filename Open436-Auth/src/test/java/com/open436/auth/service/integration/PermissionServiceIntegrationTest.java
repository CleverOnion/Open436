package com.open436.auth.service.integration;

import com.open436.auth.base.BaseIntegrationTest;
import com.open436.auth.entity.Permission;
import com.open436.auth.repository.PermissionRepository;
import com.open436.auth.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PermissionService 集成测试
 * 测试权限服务与Redis缓存的集成
 */
class PermissionServiceIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired(required = false)
    private CacheManager cacheManager;
    
    @Test
    void testUserPermissionsCache() {
        // Given: test_admin用户（ID 9001）
        Long userId = 9001L;
        
        // When: 第一次查询权限（应该从数据库查询）
        List<Permission> firstCall = permissionService.getUserPermissions(userId);
        
        // 第二次查询（应该从缓存获取）
        List<Permission> secondCall = permissionService.getUserPermissions(userId);
        
        // Then: 两次结果应该一致
        assertThat(firstCall).isNotNull();
        assertThat(secondCall).isNotNull();
        assertThat(firstCall.size()).isEqualTo(secondCall.size());
        
        // 注意：在实际场景中，可以通过监控数据库查询次数来验证缓存是否生效
        // 这里只验证数据一致性
    }
    
    @Test
    void testClearCache() {
        // Given: 用户有权限缓存
        Long userId = 9001L;
        List<Permission> beforeClear = permissionService.getUserPermissions(userId);
        
        // When: 清除缓存
        permissionService.clearUserPermissionsCache(userId);
        
        // Then: 再次查询应该从数据库重新获取
        List<Permission> afterClear = permissionService.getUserPermissions(userId);
        
        // 验证数据一致性
        assertThat(afterClear).isNotNull();
        assertThat(afterClear.size()).isEqualTo(beforeClear.size());
    }
    
    @Test
    void testGetUserPermissionCodes() {
        // Given: test_admin用户
        Long userId = 9001L;
        
        // When: 获取权限代码
        List<String> codes = permissionService.getUserPermissionCodes(userId);
        
        // Then: 应该返回权限代码列表
        assertThat(codes).isNotNull();
        // 权限数量取决于role_permissions表的数据
    }
    
    @Test
    void testHasPermission() {
        // Given: test_admin用户及其权限
        Long userId = 9001L;
        
        // 先获取用户的所有权限
        List<Permission> permissions = permissionService.getUserPermissions(userId);
        
        if (!permissions.isEmpty()) {
            // When: 检查用户是否拥有第一个权限
            String permissionCode = permissions.get(0).getCode();
            boolean hasPermission = permissionService.hasPermission(userId, permissionCode);
            
            // Then: 应该返回true
            assertThat(hasPermission).isTrue();
        }
        
        // When: 检查不存在的权限
        boolean hasNonexistent = permissionService.hasPermission(userId, "nonexistent:permission");
        
        // Then: 应该返回false
        assertThat(hasNonexistent).isFalse();
    }
}

