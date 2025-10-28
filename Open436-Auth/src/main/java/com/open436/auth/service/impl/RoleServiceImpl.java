package com.open436.auth.service.impl;

import com.open436.auth.entity.Role;
import com.open436.auth.entity.UserAuth;
import com.open436.auth.enums.ErrorCode;
import com.open436.auth.exception.BusinessException;
import com.open436.auth.repository.UserAuthRepository;
import com.open436.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    
    private final UserAuthRepository userAuthRepository;
    
    /**
     * 获取用户的角色代码列表（带缓存）
     * 缓存 Key: userRoles::userId
     * TTL: 30分钟（在 Redis 配置中设置）
     */
    @Override
    @Cacheable(value = "userRoles", key = "#userId")
    public List<String> getUserRoleCodes(Long userId) {
        log.debug("查询用户角色: userId={}", userId);
        
        UserAuth user = userAuthRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        return user.getRoles().stream()
            .map(Role::getCode)
            .collect(Collectors.toList());
    }
    
    /**
     * 清除用户角色缓存
     */
    @Override
    @CacheEvict(value = "userRoles", key = "#userId")
    public void clearUserRolesCache(Long userId) {
        log.info("清除用户角色缓存: userId={}", userId);
    }
}

