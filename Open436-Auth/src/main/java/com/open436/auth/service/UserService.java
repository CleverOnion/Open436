package com.open436.auth.service;

import com.open436.auth.dto.CreateUserRequest;
import com.open436.auth.dto.UpdatePasswordRequest;
import com.open436.auth.dto.UserInfoResponse;

/**
 * 用户管理服务接口
 */
public interface UserService {
    
    /**
     * 创建用户（管理员功能）
     * @param request 创建用户请求
     * @return 用户信息
     */
    UserInfoResponse createUser(CreateUserRequest request);
    
    /**
     * 启用/禁用用户（管理员功能）
     * @param userId 用户ID
     * @param status 新状态（active/disabled）
     * @return 用户信息
     */
    UserInfoResponse updateUserStatus(Long userId, String status);
    
    /**
     * 修改密码（用户自己）
     * @param request 修改密码请求
     */
    void updatePassword(UpdatePasswordRequest request);
    
    /**
     * 重置用户密码（管理员功能）
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    void resetPassword(Long userId, String newPassword);
}


