package com.open436.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.open436.auth.dto.ApiResponse;
import com.open436.auth.dto.LoginRequest;
import com.open436.auth.dto.LoginResponse;
import com.open436.auth.dto.UserInfoResponse;
import com.open436.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理登录、登出等认证相关请求
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        
        log.info("登录请求: username={}", request.getUsername());
        
        LoginResponse response = authService.login(request);
        
        return ResponseEntity.ok(
            ApiResponse.<LoginResponse>builder()
                .code(200)
                .message("登录成功")
                .data(response)
                .timestamp(System.currentTimeMillis())
                .build()
        );
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @SaCheckLogin
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.info("登出请求");
        
        authService.logout();
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .code(200)
                .message("已成功退出登录")
                .timestamp(System.currentTimeMillis())
                .build()
        );
    }
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    @SaCheckLogin
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        log.info("获取当前用户信息");
        
        UserInfoResponse userInfo = authService.getCurrentUser();
        
        return ResponseEntity.ok(
            ApiResponse.<UserInfoResponse>builder()
                .code(200)
                .message("获取成功")
                .data(userInfo)
                .timestamp(System.currentTimeMillis())
                .build()
        );
    }
}


