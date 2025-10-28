package com.open436.auth.e2e;

import cn.dev33.satoken.stp.StpUtil;
import com.open436.auth.base.BaseApiTest;
import com.open436.auth.dto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 端到端认证流程测试
 * 测试完整的业务场景
 */
class E2EAuthFlowTest extends BaseApiTest {
    
    @AfterEach
    void tearDown() {
        // 清理Sa-Token会话
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }
    
    @Test
    void testCompleteAuthFlow() throws Exception {
        // ===== 场景1: 管理员登录 =====
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setUsername("test_admin");
        adminLogin.setPassword("test123");
        
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(adminLogin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String adminToken = objectMapper.readTree(loginResponse).get("data").get("token").asText();
        assertThat(adminToken).isNotNull();
        
        // ===== 场景2: 管理员创建新用户 =====
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setUsername("e2e_test_user");
        createRequest.setPassword("initial123");
        createRequest.setRole("user");
        
        MvcResult createResult = mockMvc.perform(post("/api/auth/users")
                .header("token", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.username").value("e2e_test_user"))
            .andReturn();
        
        // 提取新用户ID
        String createResponse = createResult.getResponse().getContentAsString();
        assertThat(createResponse).contains("e2e_test_user");
        
        // ===== 场景3: 管理员登出 =====
        mockMvc.perform(post("/api/auth/logout")
                .header("token", adminToken))
            .andExpect(status().isOk());
        
        assertThat(StpUtil.isLogin()).isFalse();
        
        // ===== 场景4: 新用户登录 =====
        LoginRequest newUserLogin = new LoginRequest();
        newUserLogin.setUsername("e2e_test_user");
        newUserLogin.setPassword("initial123");
        
        String newUserLoginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(newUserLogin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.user.username").value("e2e_test_user"))
            .andExpect(jsonPath("$.data.user.role").value("user"))
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String newUserToken = objectMapper.readTree(newUserLoginResponse).get("data").get("token").asText();
        
        // ===== 场景5: 新用户获取自己的信息 =====
        mockMvc.perform(get("/api/auth/current")
                .header("token", newUserToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value("e2e_test_user"))
            .andExpect(jsonPath("$.data.status").value("active"));
        
        // ===== 场景6: 新用户查看自己的权限 =====
        mockMvc.perform(get("/api/auth/permissions/my")
                .header("token", newUserToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.permissions").exists());
        
        // ===== 场景7: 新用户修改密码 =====
        UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest();
        updatePasswordRequest.setOldPassword("initial123");
        updatePasswordRequest.setNewPassword("newpass456");
        updatePasswordRequest.setConfirmPassword("newpass456");
        
        mockMvc.perform(put("/api/auth/users/password")
                .header("token", newUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updatePasswordRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("密码修改成功，请重新登录"));
        
        // 密码修改后应该被踢出
        assertThat(StpUtil.isLogin()).isFalse();
        
        // ===== 场景8: 使用旧密码登录失败 =====
        LoginRequest oldPasswordLogin = new LoginRequest();
        oldPasswordLogin.setUsername("e2e_test_user");
        oldPasswordLogin.setPassword("initial123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(oldPasswordLogin)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("用户名或密码错误"));
        
        // ===== 场景9: 使用新密码重新登录 =====
        LoginRequest newPasswordLogin = new LoginRequest();
        newPasswordLogin.setUsername("e2e_test_user");
        newPasswordLogin.setPassword("newpass456");
        
        String finalLoginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(newPasswordLogin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.user.username").value("e2e_test_user"))
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        // ===== 场景10: 用户登出 =====
        String finalToken = objectMapper.readTree(finalLoginResponse).get("data").get("token").asText();
        mockMvc.perform(post("/api/auth/logout")
                .header("token", finalToken))
            .andExpect(status().isOk());
        
        assertThat(StpUtil.isLogin()).isFalse();
    }
    
    @Test
    void testUserDisableFlow() throws Exception {
        // ===== 场景1: 普通用户登录 =====
        LoginRequest userLogin = new LoginRequest();
        userLogin.setUsername("test_user");
        userLogin.setPassword("test123");
        
        String userLoginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(userLogin)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String userToken = objectMapper.readTree(userLoginResponse).get("data").get("token").asText();
        Long userId = objectMapper.readTree(userLoginResponse).get("data").get("user").get("id").asLong();
        
        // ===== 场景2: 验证用户已登录 =====
        mockMvc.perform(get("/api/auth/current")
                .header("token", userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value("test_user"));
        
        // ===== 场景3: 用户登出 =====
        mockMvc.perform(post("/api/auth/logout")
                .header("token", userToken))
            .andExpect(status().isOk());
        
        // ===== 场景4: 管理员登录 =====
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setUsername("test_admin");
        adminLogin.setPassword("test123");
        
        String adminLoginResp = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(adminLogin)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String adminToken = objectMapper.readTree(adminLoginResp).get("data").get("token").asText();
        
        // ===== 场景5: 管理员禁用用户 =====
        UpdateUserStatusRequest disableRequest = new UpdateUserStatusRequest();
        disableRequest.setStatus("disabled");
        
        mockMvc.perform(put("/api/auth/users/" + userId + "/status")
                .header("token", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(disableRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("disabled"));
        
        // ===== 场景6: 管理员登出 =====
        mockMvc.perform(post("/api/auth/logout")
                .header("token", adminToken))
            .andExpect(status().isOk());
        
        // ===== 场景7: 被禁用的用户尝试登录失败 =====
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(userLogin)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("账号已被禁用，请联系管理员"));
        
        // ===== 清理：恢复用户状态 =====
        // 管理员重新登录
        String adminReloginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(adminLogin)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        adminToken = objectMapper.readTree(adminReloginResponse).get("data").get("token").asText();
        
        // 恢复用户状态为active
        UpdateUserStatusRequest enableRequest = new UpdateUserStatusRequest();
        enableRequest.setStatus("active");
        
        mockMvc.perform(put("/api/auth/users/" + userId + "/status")
                .header("token", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(enableRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("active"));
    }
    
    @Test
    void testPermissionDeniedFlow() throws Exception {
        // ===== 场景1: 普通用户登录 =====
        LoginRequest userLogin = new LoginRequest();
        userLogin.setUsername("test_user");
        userLogin.setPassword("test123");
        
        String userTokenResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(userLogin)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String userToken = objectMapper.readTree(userTokenResponse).get("data").get("token").asText();
        
        // ===== 场景2: 普通用户尝试创建用户（需要管理员权限） =====
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setUsername("should_fail");
        createRequest.setPassword("password123");
        createRequest.setRole("user");
        
        mockMvc.perform(post("/api/auth/users")
                .header("token", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(createRequest)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(40301003))
            .andExpect(jsonPath("$.message").value("需要管理员权限"));
        
        // ===== 场景3: 普通用户尝试更新其他用户状态（需要管理员权限） =====
        UpdateUserStatusRequest statusRequest = new UpdateUserStatusRequest();
        statusRequest.setStatus("disabled");
        
        mockMvc.perform(put("/api/auth/users/9001/status")
                .header("token", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(statusRequest)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(40301003));
        
        // ===== 场景4: 普通用户尝试重置其他用户密码（需要管理员权限） =====
        String resetBody = "{\"newPassword\": \"resetpass123\"}";
        
        mockMvc.perform(put("/api/auth/users/9001/password")
                .header("token", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetBody))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(40301003));
    }
    
    @Test
    void testPasswordResetByAdminFlow() throws Exception {
        // ===== 场景1: 管理员登录 =====
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setUsername("test_admin");
        adminLogin.setPassword("test123");
        
        String adminTokenResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(adminLogin)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String adminToken = objectMapper.readTree(adminTokenResponse).get("data").get("token").asText();
        
        // 获取test_user的ID（通过查询而非硬编码）
        Long targetUserId = userAuthRepository.findByUsername("test_user").get().getId();
        
        // ===== 场景2: 管理员重置用户密码 =====
        String resetBody = "{\"newPassword\": \"admin_reset123\"}";
        
        mockMvc.perform(put("/api/auth/users/" + targetUserId + "/password")
                .header("token", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("密码重置成功"));
        
        // ===== 场景3: 管理员登出 =====
        mockMvc.perform(post("/api/auth/logout")
                .header("token", adminToken))
            .andExpect(status().isOk());
        
        // ===== 场景4: 用户使用旧密码登录失败 =====
        LoginRequest oldPasswordLogin = new LoginRequest();
        oldPasswordLogin.setUsername("test_user");
        oldPasswordLogin.setPassword("test123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(oldPasswordLogin)))
            .andExpect(status().isUnauthorized());
        
        // ===== 场景5: 用户使用新密码登录成功 =====
        LoginRequest newPasswordLogin = new LoginRequest();
        newPasswordLogin.setUsername("test_user");
        newPasswordLogin.setPassword("admin_reset123");
        
        String userNewLoginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(newPasswordLogin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.user.username").value("test_user"))
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        // ===== 清理：恢复原密码 =====
        String userToken = objectMapper.readTree(userNewLoginResponse).get("data").get("token").asText();
        
        UpdatePasswordRequest restoreRequest = new UpdatePasswordRequest();
        restoreRequest.setOldPassword("admin_reset123");
        restoreRequest.setNewPassword("test123");
        restoreRequest.setConfirmPassword("test123");
        
        mockMvc.perform(put("/api/auth/users/password")
                .header("token", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(restoreRequest)))
            .andExpect(status().isOk());
    }
}

