package com.open436.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.open436.auth.base.BaseApiTest;
import com.open436.auth.dto.LoginRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PermissionController API测试
 * 测试权限查询相关的HTTP端点
 */
class PermissionControllerTest extends BaseApiTest {
    
    @AfterEach
    void tearDown() {
        // 清理Sa-Token会话
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }
    
    @Test
    void testGetMyPermissions_Success_Returns200() throws Exception {
        // Given: 用户已登录
        String token = loginAsAdmin();
        
        // When & Then: GET /api/auth/permissions/my
        mockMvc.perform(get("/api/auth/permissions/my")
                .header("token", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("获取成功"))
            .andExpect(jsonPath("$.data.permissions").exists())
            .andExpect(jsonPath("$.data.total").exists());
    }
    
    @Test
    void testGetMyPermissions_NotLogin_Returns401() throws Exception {
        // Given: 用户未登录
        
        // When & Then: GET /api/auth/permissions/my 应该返回401
        mockMvc.perform(get("/api/auth/permissions/my"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(40101002))
            .andExpect(jsonPath("$.message").value("未登录，请先登录"));
    }
    
    @Test
    void testGetMyPermissions_AsRegularUser() throws Exception {
        // Given: 普通用户已登录
        String token = loginAsTestUser();
        
        // When & Then: 普通用户也应该能获取自己的权限
        mockMvc.perform(get("/api/auth/permissions/my")
                .header("token", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.permissions").exists())
            .andExpect(jsonPath("$.data.total").exists());
    }
    
    @Test
    void testCheckPermission_HasPermission_ReturnsTrue() throws Exception {
        // Given: 管理员已登录（假设admin角色有post:create权限）
        String token = loginAsAdmin();
        
        // When & Then: GET /api/auth/permissions/check?code=post:create
        // 注意：这取决于数据库中role_permissions表的实际数据
        mockMvc.perform(get("/api/auth/permissions/check")
                .header("token", token)
                .param("code", "post:create"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("检查成功"))
            .andExpect(jsonPath("$.data.permission").value("post:create"))
            .andExpect(jsonPath("$.data.hasPermission").exists());
    }
    
    @Test
    void testCheckPermission_NotHasPermission_ReturnsFalse() throws Exception {
        // Given: 用户已登录
        String token = loginAsTestUser();
        
        // When & Then: 检查不存在的权限
        mockMvc.perform(get("/api/auth/permissions/check")
                .header("token", token)
                .param("code", "nonexistent:permission"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.permission").value("nonexistent:permission"))
            .andExpect(jsonPath("$.data.hasPermission").value(false));
    }
    
    @Test
    void testCheckPermission_NotLogin_Returns401() throws Exception {
        // Given: 用户未登录
        
        // When & Then: 应该返回401
        mockMvc.perform(get("/api/auth/permissions/check")
                .param("code", "post:create"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(40101002))
            .andExpect(jsonPath("$.message").value("未登录，请先登录"));
    }
    
    @Test
    void testCheckPermission_MissingParameter() throws Exception {
        // Given: 用户已登录
        String token = loginAsAdmin();
        
        // When & Then: 缺少必需参数code，应该返回400
        mockMvc.perform(get("/api/auth/permissions/check")
                .header("token", token))
            .andExpect(status().isBadRequest());
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

