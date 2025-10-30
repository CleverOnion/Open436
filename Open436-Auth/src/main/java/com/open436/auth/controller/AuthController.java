package com.open436.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.open436.auth.dto.*;
import com.open436.auth.enums.TokenConstants;
import com.open436.auth.service.AuthService;
import com.open436.auth.service.RoleService;
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
    private final RoleService roleService;
    
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
    
    /**
     * 验证 Token（供 Kong Gateway 调用）
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<TokenVerifyResponse>> verifyToken(
            @RequestBody TokenVerifyRequest request) {
        
        log.debug("Token 验证请求");
        
        try {
            // 验证 Token
            Object loginId = StpUtil.getLoginIdByToken(request.getToken());
            
            if (loginId == null) {
                return ResponseEntity.ok(
                    ApiResponse.success(new TokenVerifyResponse(false, null))
                );
            }
            
            // 获取用户信息
            Long userId = Long.parseLong(loginId.toString());
            
            // 切换到该用户的上下文来获取 Session
            Object originalLoginId = StpUtil.getLoginIdDefaultNull();
            try {
                StpUtil.switchTo(userId);
                String username = (String) StpUtil.getSession().get(TokenConstants.SESSION_KEY_USERNAME);
                String role = roleService.getUserRoleCodes(userId).stream()
                    .findFirst().orElse("user");
                
                UserTokenInfo userInfo = new UserTokenInfo(userId, username, role);
                TokenVerifyResponse response = new TokenVerifyResponse(true, userInfo);
                
                log.debug("Token 验证成功: userId={}, username={}", userId, username);
                
                return ResponseEntity.ok(ApiResponse.success(response));
            } finally {
                // 恢复原来的登录状态
                if (originalLoginId != null) {
                    StpUtil.switchTo(originalLoginId);
                } else {
                    StpUtil.logout();
                }
            }
        } catch (Exception e) {
            log.warn("Token 验证失败: {}", e.getMessage());
            return ResponseEntity.ok(
                ApiResponse.success(new TokenVerifyResponse(false, null))
            );
        }
    }
}


