package com.open436.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.open436.auth.dto.CreateUserRequest;
import com.open436.auth.dto.UpdatePasswordRequest;
import com.open436.auth.dto.UserInfoResponse;
import com.open436.auth.entity.Role;
import com.open436.auth.entity.UserAuth;
import com.open436.auth.exception.BusinessException;
import com.open436.auth.repository.RoleRepository;
import com.open436.auth.repository.UserAuthRepository;
import com.open436.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserAuthRepository userAuthRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 创建用户（管理员功能）
     */
    @Override
    @Transactional
    public UserInfoResponse createUser(CreateUserRequest request) {
        log.info("创建用户: username={}, role={}", request.getUsername(), request.getRole());
        
        // 1. 检查用户名是否已存在
        if (userAuthRepository.existsByUsername(request.getUsername())) {
            log.warn("创建用户失败: 用户名已存在 - {}", request.getUsername());
            throw new BusinessException(40901001, "用户名已存在");
        }
        
        // 2. 查询角色
        Role role = roleRepository.findByCode(request.getRole())
            .orElseThrow(() -> new BusinessException(40401002, "角色不存在"));
        
        // 3. 加密密码
        String passwordHash = passwordEncoder.encode(request.getPassword());
        
        // 4. 创建用户
        UserAuth user = new UserAuth();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordHash);
        user.setStatus("active");
        user.getRoles().add(role);
        
        user = userAuthRepository.save(user);
        
        log.info("用户创建成功: userId={}, username={}, role={}", 
                 user.getId(), user.getUsername(), request.getRole());
        
        // 5. 返回用户信息
        return UserInfoResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .role(request.getRole())
            .status(user.getStatus())
            .build();
    }
    
    /**
     * 启用/禁用用户（管理员功能）
     */
    @Override
    @Transactional
    public UserInfoResponse updateUserStatus(Long userId, String status) {
        log.info("更新用户状态: userId={}, status={}", userId, status);
        
        // 1. 查询用户
        UserAuth user = userAuthRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(40401001, "用户不存在"));
        
        // 2. 更新状态
        user.setStatus(status);
        userAuthRepository.save(user);
        
        // 3. 如果是禁用操作，踢出该用户
        if ("disabled".equals(status)) {
            StpUtil.kickout(userId);
            log.info("用户已被踢出: userId={}", userId);
        }
        
        log.info("用户状态更新成功: userId={}, status={}", userId, status);
        
        // 4. 返回用户信息
        String role = user.getRoles().stream()
            .findFirst()
            .map(Role::getCode)
            .orElse("user");
        
        return UserInfoResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .role(role)
            .status(user.getStatus())
            .build();
    }
    
    /**
     * 修改密码（用户自己）
     */
    @Override
    @Transactional
    public void updatePassword(UpdatePasswordRequest request) {
        // 1. 获取当前用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("修改密码: userId={}", userId);
        
        // 2. 验证两次密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(40001004, "两次输入的密码不一致");
        }
        
        // 3. 查询用户
        UserAuth user = userAuthRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(40401001, "用户不存在"));
        
        // 4. 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            log.warn("修改密码失败: 原密码错误 - userId={}", userId);
            throw new BusinessException(40101004, "原密码错误");
        }
        
        // 5. 验证新密码不能与原密码相同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(40001005, "新密码不能与原密码相同");
        }
        
        // 6. 加密新密码并更新
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        userAuthRepository.save(user);
        
        // 7. 清除所有 Token（强制重新登录）
        StpUtil.kickout(userId);
        
        log.info("密码修改成功: userId={}", userId);
    }
    
    /**
     * 重置用户密码（管理员功能）
     */
    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        log.info("重置用户密码: userId={}", userId);
        
        // 1. 查询用户
        UserAuth user = userAuthRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(40401001, "用户不存在"));
        
        // 2. 加密新密码并更新
        String newPasswordHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(newPasswordHash);
        userAuthRepository.save(user);
        
        // 3. 清除该用户的所有 Token
        StpUtil.kickout(userId);
        
        log.info("密码重置成功: userId={}", userId);
    }
}


