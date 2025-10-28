package com.open436.auth.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.open436.auth.base.BaseUnitTest;
import com.open436.auth.dto.LoginRequest;
import com.open436.auth.dto.LoginResponse;
import com.open436.auth.dto.UserInfoResponse;
import com.open436.auth.entity.Role;
import com.open436.auth.entity.UserAuth;
import com.open436.auth.exception.BusinessException;
import com.open436.auth.repository.UserAuthRepository;
import com.open436.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AuthService 单元测试
 * 测试认证服务业务逻辑
 */
class AuthServiceTest extends BaseUnitTest {
    
    @Mock
    private UserAuthRepository userAuthRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AuthServiceImpl authService;
    
    private UserAuth mockUser;
    private Role mockRole;
    
    @BeforeEach
    void setUp() {
        // 准备测试数据
        mockRole = new Role();
        mockRole.setId(1L);
        mockRole.setCode("admin");
        mockRole.setName("管理员");
        
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
    void testLogin_Success() {
        // Given: 正确的登录请求
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        
        when(userAuthRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", mockUser.getPasswordHash())).thenReturn(true);
        when(userAuthRepository.save(any(UserAuth.class))).thenReturn(mockUser);
        
        // When & Then: 由于StpUtil是静态方法，这里只测试到密码验证
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            SaSession mockSession = mock(SaSession.class);
            stpUtilMock.when(() -> StpUtil.getSession()).thenReturn(mockSession);
            stpUtilMock.when(() -> StpUtil.getTokenValue()).thenReturn("mock_token_12345");
            
            LoginResponse response = authService.login(request);
            
            // Then: 应该登录成功
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("mock_token_12345");
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getUsername()).isEqualTo("testuser");
            
            // 验证调用
            verify(userAuthRepository).findByUsername("testuser");
            verify(passwordEncoder).matches("password123", mockUser.getPasswordHash());
            verify(userAuthRepository).save(any(UserAuth.class));
        }
    }
    
    @Test
    void testLogin_UserNotFound() {
        // Given: 用户不存在
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");
        
        when(userAuthRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // When & Then: 应该抛出异常
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("用户名或密码错误");
        
        verify(userAuthRepository).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
    
    @Test
    void testLogin_WrongPassword() {
        // Given: 密码错误
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrong_password");
        
        when(userAuthRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong_password", mockUser.getPasswordHash())).thenReturn(false);
        
        // When & Then: 应该抛出异常
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("用户名或密码错误");
        
        verify(userAuthRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrong_password", mockUser.getPasswordHash());
    }
    
    @Test
    void testLogin_AccountDisabled() {
        // Given: 账号被禁用
        mockUser.setStatus("disabled");
        
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        
        when(userAuthRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        
        // When & Then: 应该抛出异常
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("账号已被禁用");
        
        verify(userAuthRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
    
    @Test
    void testLogout_Success() {
        // Given: 用户已登录
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(() -> StpUtil.getLoginIdAsLong()).thenReturn(1L);
            
            // When: 登出
            authService.logout();
            
            // Then: 应该调用StpUtil.logout()
            stpUtilMock.verify(() -> StpUtil.logout());
        }
    }
    
    @Test
    void testGetCurrentUser_Success() {
        // Given: 用户已登录
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            SaSession mockSession = mock(SaSession.class);
            
            stpUtilMock.when(() -> StpUtil.isLogin()).thenReturn(true);
            stpUtilMock.when(() -> StpUtil.getLoginIdAsLong()).thenReturn(1L);
            stpUtilMock.when(() -> StpUtil.getSession()).thenReturn(mockSession);
            when(mockSession.get("username")).thenReturn("testuser");
            when(mockSession.get("role")).thenReturn("admin");
            when(userAuthRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            
            // When: 获取当前用户信息
            UserInfoResponse response = authService.getCurrentUser();
            
            // Then: 应该返回用户信息
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getUsername()).isEqualTo("testuser");
            assertThat(response.getRole()).isEqualTo("admin");
            assertThat(response.getStatus()).isEqualTo("active");
            
            verify(userAuthRepository).findById(1L);
        }
    }
    
    @Test
    void testGetCurrentUser_NotLogin() {
        // Given: 用户未登录
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(() -> StpUtil.isLogin()).thenReturn(false);
            
            // When & Then: 应该抛出异常
            assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未登录");
        }
    }
    
    @Test
    void testGetCurrentUser_UserNotFoundInDatabase() {
        // Given: Session中有用户信息，但数据库中不存在
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            SaSession mockSession = mock(SaSession.class);
            
            stpUtilMock.when(() -> StpUtil.isLogin()).thenReturn(true);
            stpUtilMock.when(() -> StpUtil.getLoginIdAsLong()).thenReturn(999L);
            stpUtilMock.when(() -> StpUtil.getSession()).thenReturn(mockSession);
            when(mockSession.get("username")).thenReturn("testuser");
            when(mockSession.get("role")).thenReturn("admin");
            when(userAuthRepository.findById(999L)).thenReturn(Optional.empty());
            
            // When & Then: 应该抛出异常
            assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
            
            verify(userAuthRepository).findById(999L);
        }
    }
}

