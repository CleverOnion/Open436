package com.open436.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.open436.auth.base.BaseApiTest;
import com.open436.auth.dto.LoginRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController API测试
 * 测试认证相关的HTTP端点
 */
class AuthControllerTest extends BaseApiTest {
    
    @AfterEach
    void tearDown() {
        // 清理Sa-Token会话
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }
    
    @Test
    void testLogin_Success_Returns200() throws Exception {
        // Given: 正确的登录凭证
        LoginRequest request = new LoginRequest();
        request.setUsername("test_admin");
        request.setPassword("test123");
        
        // When & Then: POST /api/auth/login
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("登录成功"))
            .andExpect(jsonPath("$.data.token").exists())
            .andExpect(jsonPath("$.data.expiresIn").value(2592000))
            .andExpect(jsonPath("$.data.user.username").value("test_admin"))
            .andExpect(jsonPath("$.data.user.role").value("admin"))
            .andExpect(jsonPath("$.data.user.status").value("active"));
    }
    
    @Test
    void testLogin_InvalidCredentials_Returns401() throws Exception {
        // Given: 错误的密码
        LoginRequest request = new LoginRequest();
        request.setUsername("test_admin");
        request.setPassword("wrong_password");
        
        // When & Then: 应该返回401
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(40101001))
            .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }
    
    @Test
    void testLogin_UserNotFound_Returns401() throws Exception {
        // Given: 不存在的用户
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent_user");
        request.setPassword("password123");
        
        // When & Then: 应该返回401
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(40101001))
            .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }
    
    @Test
    void testLogin_DisabledAccount_Returns403() throws Exception {
        // Given: 被禁用的账号
        LoginRequest request = new LoginRequest();
        request.setUsername("test_disabled");
        request.setPassword("test123");
        
        // When & Then: 应该返回403
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(40301001))
            .andExpect(jsonPath("$.message").value("账号已被禁用，请联系管理员"));
    }
    
    @Test
    void testLogin_ValidationError_Returns400() throws Exception {
        // Given: 无效的请求（用户名太短）
        LoginRequest request = new LoginRequest();
        request.setUsername("ab"); // 少于3个字符
        request.setPassword("password123");
        
        // When & Then: 应该返回400
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(40000001))
            .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void testLogin_EmptyUsername_Returns400() throws Exception {
        // Given: 空用户名
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("password123");
        
        // When & Then: 应该返回400
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(40000001));
    }
    
    @Test
    void testLogout_Success_Returns200() throws Exception {
        // Given: 用户已登录
        String token = loginAsTestUser();
        
        // When & Then: POST /api/auth/logout
        mockMvc.perform(post("/api/auth/logout")
                .header("token", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("已成功退出登录"));
    }
    
    @Test
    void testLogout_NotLogin_Returns401() throws Exception {
        // Given: 用户未登录
        
        // When & Then: POST /api/auth/logout 应该返回401
        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(40101002))
            .andExpect(jsonPath("$.message").value("未登录，请先登录"));
    }
    
    @Test
    void testGetCurrentUser_Success_Returns200() throws Exception {
        // Given: 用户已登录
        String token = loginAsTestUser();
        
        // When & Then: GET /api/auth/current
        mockMvc.perform(get("/api/auth/current")
                .header("token", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("获取成功"))
            .andExpect(jsonPath("$.data.username").value("test_user"))
            .andExpect(jsonPath("$.data.role").value("user"))
            .andExpect(jsonPath("$.data.status").value("active"));
    }
    
    @Test
    void testGetCurrentUser_NotLogin_Returns401() throws Exception {
        // Given: 用户未登录
        
        // When & Then: GET /api/auth/current 应该返回401
        mockMvc.perform(get("/api/auth/current"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(40101002))
            .andExpect(jsonPath("$.message").value("未登录，请先登录"));
    }
    
    /**
     * 辅助方法：以test_user身份登录并返回token
     */
    private String loginAsTestUser() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("test_user");
        request.setPassword("test123");
        
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        // 从响应中提取token
        return objectMapper.readTree(response).get("data").get("token").asText();
    }
}

