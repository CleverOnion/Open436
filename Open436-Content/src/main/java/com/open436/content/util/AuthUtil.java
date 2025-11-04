package com.open436.content.util;

import cn.dev33.satoken.stp.StpUtil;
import com.open436.content.common.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证工具类
 * 用于从Sa-Token中获取当前登录用户信息
 */
@Slf4j
public class AuthUtil {
    
    /**
     * 获取当前登录用户ID
     * @return 用户ID
     * @throws UnauthorizedException 如果用户未登录
     */
    public static Long getCurrentUserId() {
        if (!StpUtil.isLogin()) {
            throw new UnauthorizedException();
        }
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            log.warn("获取用户ID失败: {}", e.getMessage());
            throw new UnauthorizedException("获取用户信息失败");
        }
    }
    
    /**
     * 获取当前登录用户ID（可选，如果未登录返回null）
     * @return 用户ID，如果未登录返回null
     */
    public static Long getCurrentUserIdOrNull() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            log.warn("获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查当前用户是否为管理员
     * @return 是否为管理员
     */
    public static boolean isAdmin() {
        if (!StpUtil.isLogin()) {
            return false;
        }
        try {
            return StpUtil.hasRole("admin");
        } catch (Exception e) {
            log.warn("检查管理员权限失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查当前用户是否有指定角色
     * @param role 角色代码
     * @return 是否有该角色
     */
    public static boolean hasRole(String role) {
        if (!StpUtil.isLogin()) {
            return false;
        }
        try {
            return StpUtil.hasRole(role);
        } catch (Exception e) {
            log.warn("检查角色权限失败: {}", e.getMessage());
            return false;
        }
    }
}

