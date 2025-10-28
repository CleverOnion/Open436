package com.open436.auth.service.impl;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.open436.auth.dto.LoginRequest;
import com.open436.auth.dto.LoginResponse;
import com.open436.auth.dto.UserInfoResponse;
import com.open436.auth.entity.Role;
import com.open436.auth.entity.UserAuth;
import com.open436.auth.exception.BusinessException;
import com.open436.auth.repository.UserAuthRepository;
import com.open436.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 用户登录
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());
        
        // 1. 查询用户
        UserAuth user = userAuthRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> {
                log.warn("登录失败: 用户名不存在 - {}", request.getUsername());
                return new BusinessException(40101001, "用户名或密码错误");
            });
        
        // 2. 检查账号状态
        if ("disabled".equals(user.getStatus())) {
            log.warn("登录失败: 账号已被禁用 - {}", request.getUsername());
            throw new BusinessException(40301001, "账号已被禁用，请联系管理员");
        }
        
        // 3. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("登录失败: 密码错误 - {}", request.getUsername());
            throw new BusinessException(40101001, "用户名或密码错误");
        }
        
        // 4. 获取用户角色
        String role = user.getRoles().stream()
            .findFirst()
            .map(Role::getCode)
            .orElse("user");
        
        log.debug("用户角色: username={}, role={}", request.getUsername(), role);
        
        // 5. 使用 Sa-Token 登录（自动生成 Token 并开启自动续签）
        StpUtil.login(user.getId(), new SaLoginModel()
            .setDevice("web")
            .setIsLastingCookie(true)
            .setTimeout(2592000)  // 30天
        );
        
        // 6. 设置 Session 信息（存储在 Redis）
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("role", role);
        
        log.debug("Session 信息已设置: userId={}, username={}, role={}", 
                  user.getId(), user.getUsername(), role);
        
        // 7. 获取 Token 值
        String token = StpUtil.getTokenValue();
        
        // 8. 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userAuthRepository.save(user);
        
        log.info("登录成功: username={}, userId={}, token={}", 
                 request.getUsername(), user.getId(), token.substring(0, 10) + "...");
        
        // 9. 返回结果
        return LoginResponse.builder()
            .token(token)
            .expiresIn(2592000L)  // 30天
            .user(UserInfoResponse.from(user, role))
            .build();
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
            throw new BusinessException(40101002, "未登录");
        }
        
        // 获取用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 从 Session 获取用户名和角色
        String username = (String) StpUtil.getSession().get("username");
        String role = (String) StpUtil.getSession().get("role");
        
        // 查询用户状态
        UserAuth user = userAuthRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(40401001, "用户不存在"));
        
        return UserInfoResponse.builder()
            .id(userId)
            .username(username)
            .role(role)
            .status(user.getStatus())
            .build();
    }
}

