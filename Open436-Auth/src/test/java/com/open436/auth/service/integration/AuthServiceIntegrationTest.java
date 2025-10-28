package com.open436.auth.service.integration;

import cn.dev33.satoken.stp.StpUtil;
import com.open436.auth.base.BaseIntegrationTest;
import com.open436.auth.dto.LoginRequest;
import com.open436.auth.dto.LoginResponse;
import com.open436.auth.dto.UserInfoResponse;
import com.open436.auth.exception.BusinessException;
import com.open436.auth.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AuthService 集成测试
 * 测试认证服务与Sa-Token、Redis的集成
 */
class AuthServiceIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private AuthService authService;
    
    @AfterEach
    void tearDown() {
        // 清理Sa-Token会话
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }
    
    @Test
    void testLoginAndGetCurrentUser() {
        // Given: 正确的登录凭证
        LoginRequest request = new LoginRequest();
        request.setUsername("test_admin");
        request.setPassword("test123");
        
        // When: 登录
        LoginResponse loginResponse = authService.login(request);
        
        // Then: 应该登录成功并返回Token
        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.getToken()).isNotNull();
        assertThat(loginResponse.getUser()).isNotNull();
        assertThat(loginResponse.getUser().getUsername()).isEqualTo("test_admin");
        assertThat(loginResponse.getExpiresIn()).isEqualTo(2592000L);
        
        // Verify: 验证Sa-Token登录状态
        assertThat(StpUtil.isLogin()).isTrue();
        
        // When: 获取当前用户信息
        UserInfoResponse userInfo = authService.getCurrentUser();
        
        // Then: 应该返回正确的用户信息
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getUsername()).isEqualTo("test_admin");
        assertThat(userInfo.getRole()).isEqualTo("admin");
        assertThat(userInfo.getStatus()).isEqualTo("active");
    }
    
    @Test
    void testLogin_WrongPassword() {
        // Given: 错误的密码
        LoginRequest request = new LoginRequest();
        request.setUsername("test_admin");
        request.setPassword("wrong_password");
        
        // When & Then: 应该抛出异常
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("用户名或密码错误");
        
        // Verify: 不应该登录成功
        assertThat(StpUtil.isLogin()).isFalse();
    }
    
    @Test
    void testLogin_DisabledAccount() {
        // Given: 被禁用的账号
        LoginRequest request = new LoginRequest();
        request.setUsername("test_disabled");
        request.setPassword("test123");
        
        // When & Then: 应该抛出异常
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("账号已被禁用");
    }
    
    @Test
    void testLogoutClearsSession() {
        // Given: 用户已登录
        LoginRequest request = new LoginRequest();
        request.setUsername("test_user");
        request.setPassword("test123");
        authService.login(request);
        
        assertThat(StpUtil.isLogin()).isTrue();
        
        // When: 登出
        authService.logout();
        
        // Then: Session应该被清除
        assertThat(StpUtil.isLogin()).isFalse();
        
        // Verify: 获取当前用户应该抛出异常
        assertThatThrownBy(() -> authService.getCurrentUser())
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("未登录");
    }
    
    @Test
    void testGetCurrentUser_NotLogin() {
        // Given: 用户未登录
        
        // When & Then: 应该抛出异常
        assertThatThrownBy(() -> authService.getCurrentUser())
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("未登录");
    }
    
    @Test
    void testLogin_UpdateLastLoginTime() {
        // Given: 登录请求
        LoginRequest request = new LoginRequest();
        request.setUsername("test_user");
        request.setPassword("test123");
        
        // When: 登录
        LoginResponse response = authService.login(request);
        
        // Then: 应该更新最后登录时间
        assertThat(response).isNotNull();
        assertThat(response.getUser().getId()).isNotNull();
        
        // 验证登录成功后可以获取用户信息
        UserInfoResponse userInfo = authService.getCurrentUser();
        assertThat(userInfo.getUsername()).isEqualTo("test_user");
    }
}

