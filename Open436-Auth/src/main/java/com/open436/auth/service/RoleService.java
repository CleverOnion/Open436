package com.open436.auth.service;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {
    
    /**
     * 获取用户的角色代码列表
     * @param userId 用户ID
     * @return 角色代码列表
     */
    List<String> getUserRoleCodes(Long userId);
    
    /**
     * 清除用户角色缓存
     * @param userId 用户ID
     */
    void clearUserRolesCache(Long userId);
}

