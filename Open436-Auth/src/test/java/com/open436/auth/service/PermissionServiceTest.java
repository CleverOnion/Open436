package com.open436.auth.service;

import com.open436.auth.base.BaseUnitTest;
import com.open436.auth.entity.Permission;
import com.open436.auth.repository.PermissionRepository;
import com.open436.auth.service.impl.PermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * PermissionService 单元测试
 * 测试权限服务业务逻辑
 */
class PermissionServiceTest extends BaseUnitTest {
    
    @Mock
    private PermissionRepository permissionRepository;
    
    @InjectMocks
    private PermissionServiceImpl permissionService;
    
    private List<Permission> mockPermissions;
    
    @BeforeEach
    void setUp() {
        Permission perm1 = new Permission();
        perm1.setId(1L);
        perm1.setCode("post:create");
        perm1.setName("创建帖子");
        perm1.setResource("post");
        perm1.setAction("create");
        
        Permission perm2 = new Permission();
        perm2.setId(2L);
        perm2.setCode("post:read");
        perm2.setName("查看帖子");
        perm2.setResource("post");
        perm2.setAction("read");
        
        Permission perm3 = new Permission();
        perm3.setId(3L);
        perm3.setCode("user:manage");
        perm3.setName("管理用户");
        perm3.setResource("user");
        perm3.setAction("manage");
        
        mockPermissions = Arrays.asList(perm1, perm2, perm3);
    }
    
    @Test
    void testGetUserPermissions() {
        // Given: 用户拥有多个权限
        Long userId = 1L;
        when(permissionRepository.findByUserId(userId)).thenReturn(mockPermissions);
        
        // When: 获取用户权限
        List<Permission> result = permissionService.getUserPermissions(userId);
        
        // Then: 应该返回权限列表
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsAll(mockPermissions);
        
        verify(permissionRepository).findByUserId(userId);
    }
    
    @Test
    void testGetUserPermissions_Empty() {
        // Given: 用户没有权限
        Long userId = 999L;
        when(permissionRepository.findByUserId(userId)).thenReturn(Arrays.asList());
        
        // When: 获取用户权限
        List<Permission> result = permissionService.getUserPermissions(userId);
        
        // Then: 应该返回空列表
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        verify(permissionRepository).findByUserId(userId);
    }
    
    @Test
    void testGetUserPermissionCodes() {
        // Given: 用户拥有权限
        Long userId = 1L;
        when(permissionRepository.findByUserId(userId)).thenReturn(mockPermissions);
        
        // When: 获取权限代码列表
        List<String> codes = permissionService.getUserPermissionCodes(userId);
        
        // Then: 应该返回权限代码
        assertThat(codes).isNotNull();
        assertThat(codes).hasSize(3);
        assertThat(codes).containsExactlyInAnyOrder("post:create", "post:read", "user:manage");
        
        verify(permissionRepository).findByUserId(userId);
    }
    
    @Test
    void testGetUserPermissionCodes_Empty() {
        // Given: 用户没有权限
        Long userId = 999L;
        when(permissionRepository.findByUserId(userId)).thenReturn(Arrays.asList());
        
        // When: 获取权限代码列表
        List<String> codes = permissionService.getUserPermissionCodes(userId);
        
        // Then: 应该返回空列表
        assertThat(codes).isEmpty();
    }
    
    @Test
    void testHasPermission_True() {
        // Given: 用户拥有指定权限
        Long userId = 1L;
        String permissionCode = "post:create";
        when(permissionRepository.findByUserId(userId)).thenReturn(mockPermissions);
        
        // When: 检查权限
        boolean hasPermission = permissionService.hasPermission(userId, permissionCode);
        
        // Then: 应该返回true
        assertThat(hasPermission).isTrue();
        
        verify(permissionRepository).findByUserId(userId);
    }
    
    @Test
    void testHasPermission_False() {
        // Given: 用户没有指定权限
        Long userId = 1L;
        String permissionCode = "admin:manage";
        when(permissionRepository.findByUserId(userId)).thenReturn(mockPermissions);
        
        // When: 检查权限
        boolean hasPermission = permissionService.hasPermission(userId, permissionCode);
        
        // Then: 应该返回false
        assertThat(hasPermission).isFalse();
        
        verify(permissionRepository).findByUserId(userId);
    }
    
    @Test
    void testHasPermission_NoPermissions() {
        // Given: 用户没有任何权限
        Long userId = 999L;
        when(permissionRepository.findByUserId(userId)).thenReturn(Arrays.asList());
        
        // When: 检查权限
        boolean hasPermission = permissionService.hasPermission(userId, "post:create");
        
        // Then: 应该返回false
        assertThat(hasPermission).isFalse();
    }
    
    @Test
    void testClearUserPermissionsCache() {
        // Given: 用户权限缓存存在
        Long userId = 1L;
        
        // When: 清除缓存
        permissionService.clearUserPermissionsCache(userId);
        
        // Then: 方法应该执行成功（@CacheEvict注解会处理缓存清除）
        // 在单元测试中无法验证缓存操作，但可以验证方法调用不抛异常
    }
}

