package com.open436.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.open436.auth.base.BaseApiTest;
import com.open436.auth.dto.CreateUserRequest;
import com.open436.auth.dto.LoginRequest;
import com.open436.auth.dto.UpdatePasswordRequest;
import com.open436.auth.dto.UpdateUserStatusRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserController API测试
 * 测试用户管理相关的HTTP端点
 */
class UserControllerTest extends BaseApiTest {
    
    @AfterEach
    void tearDown() {
        // 清理Sa-Token会话
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }
    
    @Test
    void testCreateUser_AsAdmin_Returns201() throws Exception {
        // Given: 管理员已登录
        String adminToken = loginAsAdmin();
        
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("new_test_user");
        request.setPassword("password123");
        request.setRole("user");
        
        // When & Then: POST /api/auth/users
        mockMvc.perform(post("/api/auth/users")
                .header("token", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value(201))
            .andExpect(jsonPath("$.message").value("用户创建成功"))
            .andExpect(jsonPath("$.data.username").value("new_test_user"))
            .andExpect(jsonPath("$.data.role").value("user"))
            .andExpect(jsonPath("$.data.status").value("active"));
    }
    
    @Test
    void testCreateUser_AsNonAdmin_Returns403() throws Exception {
        // Given: 普通用户已登录
        String userToken = loginAsTestUser();
        
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("new_user_2");
        request.setPassword("password123");
        request.setRole("user");
        
        // When & Then: 应该返回403（需要管理员权限）
        mockMvc.perform(post("/api/auth/users")
                .header("token", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(40301003))
            .andExpect(jsonPath("$.message").value("需要管理员权限"));
    }
    
    @Test
    void testCreateUser_NotLogin_Returns401() throws Exception {
        // Given: 用户未登录
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("new_user_3");
        request.setPassword("password123");
        request.setRole("user");
        
        // When & Then: 应该返回401
        mockMvc.perform(post("/api/auth/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(40101002));
    }
    
    @Test
    void testCreateUser_ValidationError_Returns400() throws Exception {
        // Given: 管理员已登录，但请求数据无效
        String adminToken = loginAsAdmin();
        
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("ab"); // 用户名太短
        request.setPassword("123"); // 密码太短
        request.setRole("user");
        
        // When & Then: 应该返回400
        mockMvc.perform(post("/api/auth/users")
                .header("token", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(40000001));
    }
    
    @Test
    void testUpdateUserStatus_AsAdmin_Returns200() throws Exception {
        // Given: 管理员已登录
        String adminToken = loginAsAdmin();
        Long userId = userAuthRepository.findByUsername("test_user").get().getId();
        
        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus("disabled");
        
        // When & Then: PUT /api/auth/users/{id}/status
        mockMvc.perform(put("/api/auth/users/" + userId + "/status")
                .header("token", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("用户状态已更新"))
            .andExpect(jsonPath("$.data.status").value("disabled"));
    }
    
    @Test
    void testUpdateUserStatus_AsNonAdmin_Returns403() throws Exception {
        // Given: 普通用户已登录
        String userToken = loginAsTestUser();
        Long userId = userAuthRepository.findByUsername("test_disabled").get().getId();
        
        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus("disabled");
        
        // When & Then: 应该返回403
        mockMvc.perform(put("/api/auth/users/" + userId + "/status")
                .header("token", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(40301003));
    }
    
    @Test
    void testUpdatePassword_Success_Returns200() throws Exception {
        // Given: 用户已登录
        String userToken = loginAsTestUser();
        
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("test123");
        request.setNewPassword("newpass123");
        request.setConfirmPassword("newpass123");
        
        // When & Then: PUT /api/auth/users/password
        mockMvc.perform(put("/api/auth/users/password")
                .header("token", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("密码修改成功，请重新登录"));
    }
    
    @Test
    void testUpdatePassword_WrongOldPassword_Returns401() throws Exception {
        // Given: 用户已登录，但原密码错误
        String userToken = loginAsTestUser();
        
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("wrong_password");
        request.setNewPassword("newpass123");
        request.setConfirmPassword("newpass123");
        
        // When & Then: 应该返回401
        mockMvc.perform(put("/api/auth/users/password")
                .header("token", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(40101004))
            .andExpect(jsonPath("$.message").value("原密码错误"));
    }
    
    @Test
    void testUpdatePassword_ConfirmNotMatch_Returns400() throws Exception {
        // Given: 用户已登录，但两次新密码不一致
        String userToken = loginAsTestUser();
        
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("test123");
        request.setNewPassword("newpass123");
        request.setConfirmPassword("different123");
        
        // When & Then: 应该返回400
        mockMvc.perform(put("/api/auth/users/password")
                .header("token", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(40001004))
            .andExpect(jsonPath("$.message").value("两次输入的密码不一致"));
    }
    
    @Test
    void testUpdatePassword_NotLogin_Returns401() throws Exception {
        // Given: 用户未登录
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("test123");
        request.setNewPassword("newpass123");
        request.setConfirmPassword("newpass123");
        
        // When & Then: 应该返回401
        mockMvc.perform(put("/api/auth/users/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(40101002));
    }
    
    @Test
    void testResetPassword_AsAdmin_Returns200() throws Exception {
        // Given: 管理员已登录
        String adminToken = loginAsAdmin();
        Long userId = userAuthRepository.findByUsername("test_user").get().getId();
        
        String requestBody = "{\"newPassword\": \"resetpass123\"}";
        
        // When & Then: PUT /api/auth/users/{id}/password
        mockMvc.perform(put("/api/auth/users/" + userId + "/password")
                .header("token", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("密码重置成功"));
    }
    
    @Test
    void testResetPassword_AsNonAdmin_Returns403() throws Exception {
        // Given: 普通用户已登录
        String userToken = loginAsTestUser();
        Long userId = userAuthRepository.findByUsername("test_disabled").get().getId();
        
        String requestBody = "{\"newPassword\": \"resetpass123\"}";
        
        // When & Then: 应该返回403
        mockMvc.perform(put("/api/auth/users/" + userId + "/password")
                .header("token", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(40301003));
    }
    
    /**
     * 辅助方法：以管理员身份登录并返回token
     */
    private String loginAsAdmin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("test_admin");
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
    
    /**
     * 辅助方法：以普通用户身份登录并返回token
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

