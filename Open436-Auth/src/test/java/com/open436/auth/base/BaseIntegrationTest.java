package com.open436.auth.base;

import com.open436.auth.entity.Role;
import com.open436.auth.entity.UserAuth;
import com.open436.auth.repository.RoleRepository;
import com.open436.auth.repository.UserAuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 集成测试基类
 * 使用完整的Spring上下文，连接真实的数据库和Redis
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional  // 自动回滚事务，保证测试数据隔离
public abstract class BaseIntegrationTest {
    
    @Autowired(required = false)
    protected UserAuthRepository userAuthRepository;
    
    @Autowired(required = false)
    protected RoleRepository roleRepository;
    
    @Autowired(required = false)
    protected PasswordEncoder passwordEncoder;
    
    /**
     * 在每个测试方法执行前创建测试用户
     * 使用@BeforeEach确保每个测试都有干净的测试数据
     */
    @BeforeEach
    public void setUpTestUsers() {
        if (userAuthRepository == null || roleRepository == null || passwordEncoder == null) {
            return; // Skip for non-integration tests
        }
        
        // 确保角色存在
        Optional<Role> adminRoleOpt = roleRepository.findByCode("admin");
        Optional<Role> userRoleOpt = roleRepository.findByCode("user");
        
        if (adminRoleOpt.isEmpty() || userRoleOpt.isEmpty()) {
            return; // Skip if roles not initialized
        }
        
        Role adminRole = adminRoleOpt.get();
        Role userRole = userRoleOpt.get();
        
        // 删除已存在的测试用户（如果存在）
        userAuthRepository.findByUsername("test_admin").ifPresent(userAuthRepository::delete);
        userAuthRepository.findByUsername("test_user").ifPresent(userAuthRepository::delete);
        userAuthRepository.findByUsername("test_disabled").ifPresent(userAuthRepository::delete);
        
        // 创建test_admin用户
        if (userAuthRepository.findByUsername("test_admin").isEmpty()) {
            UserAuth adminUser = new UserAuth();
            adminUser.setUsername("test_admin");
            adminUser.setPasswordHash(passwordEncoder.encode("test123"));
            adminUser.setStatus("active");
            adminUser.getRoles().add(adminRole);
            userAuthRepository.saveAndFlush(adminUser);
        }
        
        // 创建test_user用户
        if (userAuthRepository.findByUsername("test_user").isEmpty()) {
            UserAuth testUser = new UserAuth();
            testUser.setUsername("test_user");
            testUser.setPasswordHash(passwordEncoder.encode("test123"));
            testUser.setStatus("active");
            testUser.getRoles().add(userRole);
            userAuthRepository.saveAndFlush(testUser);
        }
        
        // 创建test_disabled用户
        if (userAuthRepository.findByUsername("test_disabled").isEmpty()) {
            UserAuth disabledUser = new UserAuth();
            disabledUser.setUsername("test_disabled");
            disabledUser.setPasswordHash(passwordEncoder.encode("test123"));
            disabledUser.setStatus("disabled");
            disabledUser.getRoles().add(userRole);
            userAuthRepository.saveAndFlush(disabledUser);
        }
    }
}

