package com.open436.auth.service;

import com.open436.auth.dto.LoginRequest;
import com.open436.auth.dto.LoginResponse;
import com.open436.auth.dto.UserInfoResponse;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录响应（包含 Token 和用户信息）
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 用户登出
     */
    void logout();
    
    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    UserInfoResponse getCurrentUser();
}

