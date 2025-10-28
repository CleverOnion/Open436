package com.open436.auth.service.impl;

import com.open436.auth.entity.Permission;
import com.open436.auth.repository.PermissionRepository;
import com.open436.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    
    private final PermissionRepository permissionRepository;
    
    /**
     * 获取用户的所有权限（带缓存）
     * 缓存 Key: userPermissions::userId
     * TTL: 30分钟（在 Redis 配置中设置）
     */
    @Override
    @Cacheable(value = "userPermissions", key = "#userId")
    public List<Permission> getUserPermissions(Long userId) {
        log.debug("查询用户权限: userId={}", userId);
        return permissionRepository.findByUserId(userId);
    }
    
    /**
     * 获取用户的所有权限代码
     */
    @Override
    public List<String> getUserPermissionCodes(Long userId) {
        return getUserPermissions(userId).stream()
            .map(Permission::getCode)
            .collect(Collectors.toList());
    }
    
    /**
     * 检查用户是否拥有指定权限
     */
    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        List<Permission> permissions = getUserPermissions(userId);
        return permissions.stream()
            .anyMatch(p -> p.getCode().equals(permissionCode));
    }
    
    /**
     * 清除用户权限缓存
     */
    @Override
    @CacheEvict(value = "userPermissions", key = "#userId")
    public void clearUserPermissionsCache(Long userId) {
        log.info("清除用户权限缓存: userId={}", userId);
    }
}


