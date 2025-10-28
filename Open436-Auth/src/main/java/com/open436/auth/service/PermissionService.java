package com.open436.auth.service;

import com.open436.auth.entity.Permission;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService {
    
    /**
     * 获取用户的所有权限
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> getUserPermissions(Long userId);
    
    /**
     * 获取用户的所有权限代码
     * @param userId 用户ID
     * @return 权限代码列表
     */
    List<String> getUserPermissionCodes(Long userId);
    
    /**
     * 检查用户是否拥有指定权限
     * @param userId 用户ID
     * @param permissionCode 权限代码
     * @return 是否拥有权限
     */
    boolean hasPermission(Long userId, String permissionCode);
    
    /**
     * 清除用户权限缓存
     * @param userId 用户ID
     */
    void clearUserPermissionsCache(Long userId);
    
    /**
     * 清除所有用户权限缓存
     */
    void clearAllPermissionsCache();
}


