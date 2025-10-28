package com.open436.auth.service.impl;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.open436.auth.config.TokenProperties;
import com.open436.auth.dto.LoginRequest;
import com.open436.auth.dto.LoginResponse;
import com.open436.auth.dto.UserInfoResponse;
import com.open436.auth.entity.UserAuth;
import com.open436.auth.enums.ErrorCode;
import com.open436.auth.enums.TokenConstants;
import com.open436.auth.enums.UserStatus;
import com.open436.auth.exception.BusinessException;
import com.open436.auth.repository.UserAuthRepository;
import com.open436.auth.service.AuthService;
import com.open436.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final TokenProperties tokenProperties;
    
    /**
     * 用户登录
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());
        
        // 1. 认证用户（数据库操作，在事务中）
        UserAuth user = authenticateUser(request);
        String role = user.getPrimaryRoleCode();
        
        // 2. 创建会话（外部状态操作，不在事务中）
        String token = createSession(user.getId(), user.getUsername(), role);
        
        log.info("登录成功: username={}, userId={}", request.getUsername(), user.getId());
        
        // 3. 返回结果
        return LoginResponse.builder()
            .token(token)
            .expiresIn(tokenProperties.getTimeout())
            .user(UserInfoResponse.from(user, role))
            .build();
    }
    
    /**
     * 认证用户（数据库操作）
     * @param request 登录请求
     * @return 用户实体
     */
    @Transactional
    private UserAuth authenticateUser(LoginRequest request) {
        // 1. 查询用户
        UserAuth user = userAuthRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> {
                log.warn("登录失败: 用户名不存在 - {}", request.getUsername());
                return new BusinessException(ErrorCode.INVALID_CREDENTIALS);
            });
        
        // 2. 检查账号状态
        if (UserStatus.DISABLED.getCode().equals(user.getStatus())) {
            log.warn("登录失败: 账号已被禁用 - {}", request.getUsername());
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        
        // 3. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("登录失败: 密码错误 - {}", request.getUsername());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        
        // 4. 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userAuthRepository.save(user);
        
        log.debug("用户认证成功: username={}, userId={}", request.getUsername(), user.getId());
        
        return user;
    }
    
    /**
     * 创建用户会话（Sa-Token操作）
     * @param userId 用户ID
     * @param username 用户名
     * @param role 角色代码
     * @return Token值
     */
    private String createSession(Long userId, String username, String role) {
        // 1. 使用 Sa-Token 登录（自动生成 Token 并开启自动续签）
        StpUtil.login(userId, new SaLoginModel()
            .setDevice(TokenConstants.DEVICE_WEB)
            .setIsLastingCookie(true)
            .setTimeout(tokenProperties.getTimeout())
        );
        
        // 2. 设置 Session 信息（存储在 Redis）
        // 注：角色信息不再存储在Session中，改为从数据库查询（通过RoleService）
        StpUtil.getSession().set(TokenConstants.SESSION_KEY_USERNAME, username);
        
        log.debug("Session 信息已设置: userId={}, username={}, role={}", userId, username, role);
        
        // 3. 获取并返回 Token 值
        return StpUtil.getTokenValue();
    }
    
    /**
     * 用户登出
     */
    @Override
    public void logout() {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("用户登出: userId={}", userId);
        
        // Sa-Token 登出（自动清除 Session 和 Token）
        StpUtil.logout();
        
        log.info("登出成功: userId={}", userId);
    }
    
    /**
     * 获取当前登录用户信息
     */
    @Override
    public UserInfoResponse getCurrentUser() {
        // 检查是否登录
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.NOT_LOGGED_IN);
        }
        
        // 获取用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 从 Session 获取用户名
        String username = (String) StpUtil.getSession().get(TokenConstants.SESSION_KEY_USERNAME);
        
        // 从数据库查询角色（带缓存）
        List<String> roles = roleService.getUserRoleCodes(userId);
        String role = roles.isEmpty() ? TokenConstants.DEFAULT_ROLE : roles.get(0);
        
        // 查询用户状态
        UserAuth user = userAuthRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        return UserInfoResponse.builder()
            .id(userId)
            .username(username)
            .role(role)
            .status(user.getStatus())
            .build();
    }
}

