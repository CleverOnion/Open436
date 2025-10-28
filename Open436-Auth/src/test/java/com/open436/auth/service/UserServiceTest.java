package com.open436.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.open436.auth.base.BaseUnitTest;
import com.open436.auth.dto.CreateUserRequest;
import com.open436.auth.dto.UpdatePasswordRequest;
import com.open436.auth.dto.UserInfoResponse;
import com.open436.auth.entity.Role;
import com.open436.auth.entity.UserAuth;
import com.open436.auth.exception.BusinessException;
import com.open436.auth.repository.RoleRepository;
import com.open436.auth.repository.UserAuthRepository;
import com.open436.auth.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 * 测试用户管理服务业务逻辑
 */
class UserServiceTest extends BaseUnitTest {
    
    @Mock
    private UserAuthRepository userAuthRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    private UserAuth mockUser;
    private Role mockRole;
    
    @BeforeEach
    void setUp() {
        mockRole = new Role();
        mockRole.setId(1L);
        mockRole.setCode("user");
        mockRole.setName("普通用户");
        
        Set<Role> roles = new HashSet<>();
        roles.add(mockRole);
        
        mockUser = new UserAuth();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPasswordHash("$2a$10$hashed_password");
        mockUser.setStatus("active");
        mockUser.setRoles(roles);
    }
    
    @Test
    void testCreateUser_Success() {
        // Given: 创建用户请求
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRole("user");
        
        when(userAuthRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findByCode("user")).thenReturn(Optional.of(mockRole));
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded_hash");
        
        UserAuth savedUser = new UserAuth();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setPasswordHash("$2a$10$encoded_hash");
        savedUser.setStatus("active");
        savedUser.setRoles(new HashSet<>());
        savedUser.getRoles().add(mockRole);
        
        when(userAuthRepository.save(any(UserAuth.class))).thenReturn(savedUser);
        
        // When: 创建用户
        UserInfoResponse response = userService.createUser(request);
        
        // Then: 应该创建成功
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getRole()).isEqualTo("user");
        assertThat(response.getStatus()).isEqualTo("active");
        
        verify(userAuthRepository).existsByUsername("newuser");
        verify(roleRepository).findByCode("user");
        verify(passwordEncoder).encode("password123");
        verify(userAuthRepository).save(any(UserAuth.class));
    }
    
    @Test
    void testCreateUser_UsernameExists() {
        // Given: 用户名已存在
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");
        request.setRole("user");
        
        when(userAuthRepository.existsByUsername("existinguser")).thenReturn(true);
        
        // When & Then: 应该抛出异常
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("用户名已存在");
        
        verify(userAuthRepository).existsByUsername("existinguser");
        verify(roleRepository, never()).findByCode(anyString());
        verify(userAuthRepository, never()).save(any());
    }
    
    @Test
    void testCreateUser_RoleNotFound() {
        // Given: 角色不存在
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRole("nonexistent_role");
        
        when(userAuthRepository.existsByUsername("newuser")).thenReturn(false);
        when(roleRepository.findByCode("nonexistent_role")).thenReturn(Optional.empty());
        
        // When & Then: 应该抛出异常
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("角色不存在");
        
        verify(userAuthRepository).existsByUsername("newuser");
        verify(roleRepository).findByCode("nonexistent_role");
        verify(userAuthRepository, never()).save(any());
    }
    
    @Test
    void testUpdateUserStatus_Success() {
        // Given: 更新用户状态
        Long userId = 1L;
        String newStatus = "disabled";
        
        when(userAuthRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userAuthRepository.save(any(UserAuth.class))).thenReturn(mockUser);
        
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            // When: 更新状态
            UserInfoResponse response = userService.updateUserStatus(userId, newStatus);
            
            // Then: 应该更新成功
            assertThat(response).isNotNull();
            assertThat(mockUser.getStatus()).isEqualTo("disabled");
            
            // 验证调用了kickout
            stpUtilMock.verify(() -> StpUtil.kickout(userId));
            
            verify(userAuthRepository).findById(userId);
            verify(userAuthRepository).save(mockUser);
        }
    }
    
    @Test
    void testUpdateUserStatus_UserNotFound() {
        // Given: 用户不存在
        Long userId = 999L;
        
        when(userAuthRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then: 应该抛出异常
        assertThatThrownBy(() -> userService.updateUserStatus(userId, "disabled"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("用户不存在");
        
        verify(userAuthRepository).findById(userId);
        verify(userAuthRepository, never()).save(any());
    }
    
    @Test
    void testUpdateUserStatus_Active_ShouldNotKickout() {
        // Given: 更新为active状态
        Long userId = 1L;
        String newStatus = "active";
        
        when(userAuthRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userAuthRepository.save(any(UserAuth.class))).thenReturn(mockUser);
        
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            // When: 更新状态为active
            userService.updateUserStatus(userId, newStatus);
            
            // Then: 不应该调用kickout
            stpUtilMock.verify(() -> StpUtil.kickout(userId), never());
        }
    }
    
    @Test
    void testUpdatePassword_Success() {
        // Given: 修改密码请求
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("oldpass123");
        request.setNewPassword("newpass123");
        request.setConfirmPassword("newpass123");
        
        when(userAuthRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldpass123", mockUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.matches("newpass123", mockUser.getPasswordHash())).thenReturn(false);
        when(passwordEncoder.encode("newpass123")).thenReturn("$2a$10$new_hash");
        when(userAuthRepository.save(any(UserAuth.class))).thenReturn(mockUser);
        
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(() -> StpUtil.getLoginIdAsLong()).thenReturn(1L);
            
            // When: 修改密码
            userService.updatePassword(request);
            
            // Then: 应该修改成功并踢出用户
            verify(passwordEncoder, atLeastOnce()).matches(anyString(), anyString());
            verify(passwordEncoder).encode("newpass123");
            verify(userAuthRepository).save(mockUser);
            stpUtilMock.verify(() -> StpUtil.kickout(1L));
        }
    }
    
    @Test
    void testUpdatePassword_WrongOldPassword() {
        // Given: 原密码错误
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("wrongpass");
        request.setNewPassword("newpass123");
        request.setConfirmPassword("newpass123");
        
        when(userAuthRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongpass", mockUser.getPasswordHash())).thenReturn(false);
        
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(() -> StpUtil.getLoginIdAsLong()).thenReturn(1L);
            
            // When & Then: 应该抛出异常
            assertThatThrownBy(() -> userService.updatePassword(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("原密码错误");
            
            verify(passwordEncoder, never()).encode(anyString());
            verify(userAuthRepository, never()).save(any());
        }
    }
    
    @Test
    void testUpdatePassword_SameAsOld() {
        // Given: 新密码与原密码相同
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("oldpass123");
        request.setNewPassword("oldpass123");
        request.setConfirmPassword("oldpass123");
        
        when(userAuthRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldpass123", mockUser.getPasswordHash())).thenReturn(true);
        
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(() -> StpUtil.getLoginIdAsLong()).thenReturn(1L);
            
            // When & Then: 应该抛出异常
            assertThatThrownBy(() -> userService.updatePassword(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("新密码不能与原密码相同");
        }
    }
    
    @Test
    void testUpdatePassword_ConfirmNotMatch() {
        // Given: 两次输入的新密码不一致
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("oldpass123");
        request.setNewPassword("newpass123");
        request.setConfirmPassword("different123");
        
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(() -> StpUtil.getLoginIdAsLong()).thenReturn(1L);
            
            // When & Then: 应该抛出异常
            assertThatThrownBy(() -> userService.updatePassword(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("两次输入的密码不一致");
            
            verify(userAuthRepository, never()).findById(any());
        }
    }
    
    @Test
    void testResetPassword_Success() {
        // Given: 重置密码
        Long userId = 1L;
        String newPassword = "resetpass123";
        
        when(userAuthRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("$2a$10$reset_hash");
        when(userAuthRepository.save(any(UserAuth.class))).thenReturn(mockUser);
        
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            // When: 重置密码
            userService.resetPassword(userId, newPassword);
            
            // Then: 应该重置成功并踢出用户
            verify(passwordEncoder).encode(newPassword);
            verify(userAuthRepository).save(mockUser);
            stpUtilMock.verify(() -> StpUtil.kickout(userId));
        }
    }
    
    @Test
    void testResetPassword_UserNotFound() {
        // Given: 用户不存在
        Long userId = 999L;
        
        when(userAuthRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then: 应该抛出异常
        assertThatThrownBy(() -> userService.resetPassword(userId, "newpass123"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("用户不存在");
        
        verify(userAuthRepository).findById(userId);
        verify(passwordEncoder, never()).encode(anyString());
    }
}

