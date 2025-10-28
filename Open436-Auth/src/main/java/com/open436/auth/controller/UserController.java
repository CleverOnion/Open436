package com.open436.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.open436.auth.dto.*;
import com.open436.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * 处理用户管理相关请求
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 创建用户（管理员功能）
     */
    @PostMapping
    @SaCheckRole("admin")
    public ResponseEntity<ApiResponse<UserInfoResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        
        log.info("创建用户请求: username={}", request.getUsername());
        
        UserInfoResponse response = userService.createUser(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.<UserInfoResponse>builder()
                .code(201)
                .message("用户创建成功")
                .data(response)
                .timestamp(System.currentTimeMillis())
                .build());
    }
    
    /**
     * 启用/禁用用户（管理员功能）
     */
    @PutMapping("/{id}/status")
    @SaCheckRole("admin")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        
        log.info("更新用户状态请求: userId={}, status={}", id, request.getStatus());
        
        UserInfoResponse response = userService.updateUserStatus(id, request.getStatus());
        
        return ResponseEntity.ok(
            ApiResponse.<UserInfoResponse>builder()
                .code(200)
                .message("用户状态已更新")
                .data(response)
                .timestamp(System.currentTimeMillis())
                .build()
        );
    }
    
    /**
     * 修改密码（用户自己）
     */
    @PutMapping("/password")
    @SaCheckLogin
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request) {
        
        log.info("修改密码请求");
        
        userService.updatePassword(request);
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .code(200)
                .message("密码修改成功，请重新登录")
                .timestamp(System.currentTimeMillis())
                .build()
        );
    }
    
    /**
     * 重置用户密码（管理员功能）
     */
    @PutMapping("/{id}/password")
    @SaCheckRole("admin")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @RequestBody ResetPasswordRequest request) {
        
        log.info("重置用户密码请求: userId={}", id);
        
        userService.resetPassword(id, request.getNewPassword());
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .code(200)
                .message("密码重置成功")
                .timestamp(System.currentTimeMillis())
                .build()
        );
    }
}


