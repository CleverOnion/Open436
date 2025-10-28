package com.open436.auth.repository;

import com.open436.auth.base.BaseIntegrationTest;
import com.open436.auth.entity.UserAuth;
import com.open436.auth.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserAuthRepository 集成测试
 * 测试用户认证数据访问层
 */
class UserAuthRepositoryTest extends BaseIntegrationTest {
    
    @Autowired
    private UserAuthRepository userAuthRepository;
    
    @Test
    void testFindByUsername_Success() {
        // Given: 测试数据中存在test_admin用户
        
        // When: 根据用户名查询
        Optional<UserAuth> result = userAuthRepository.findByUsername("test_admin");
        
        // Then: 应该查询成功
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("test_admin");
        assertThat(result.get().getStatus()).isEqualTo(UserStatus.ACTIVE.getCode());
    }
    
    @Test
    void testFindByUsername_NotFound() {
        // Given: 不存在的用户名
        String username = "nonexistent_user";
        
        // When: 查询用户
        Optional<UserAuth> result = userAuthRepository.findByUsername(username);
        
        // Then: 应该返回空
        assertThat(result).isEmpty();
    }
    
    @Test
    void testExistsByUsername_True() {
        // Given: 测试数据中存在test_user
        
        // When: 检查用户名是否存在
        boolean exists = userAuthRepository.existsByUsername("test_user");
        
        // Then: 应该返回true
        assertThat(exists).isTrue();
    }
    
    @Test
    void testExistsByUsername_False() {
        // Given: 不存在的用户名
        
        // When: 检查用户名是否存在
        boolean exists = userAuthRepository.existsByUsername("nonexistent_user");
        
        // Then: 应该返回false
        assertThat(exists).isFalse();
    }
    
    @Test
    void testFindByStatus_Active() {
        // Given: 测试数据中有active和disabled用户
        
        // When: 查询所有活跃用户
        List<UserAuth> activeUsers = userAuthRepository.findByStatus(UserStatus.ACTIVE.getCode());
        
        // Then: 应该只包含active用户
        assertThat(activeUsers).isNotEmpty();
        assertThat(activeUsers).allMatch(user -> UserStatus.ACTIVE.getCode().equals(user.getStatus()));
    }
    
    @Test
    void testFindByStatus_Disabled() {
        // Given: 测试数据中有disabled用户
        
        // When: 查询所有禁用用户
        List<UserAuth> disabledUsers = userAuthRepository.findByStatus(UserStatus.DISABLED.getCode());
        
        // Then: 应该包含test_disabled用户
        assertThat(disabledUsers).isNotEmpty();
        assertThat(disabledUsers).anyMatch(user -> "test_disabled".equals(user.getUsername()));
    }
    
    @Test
    void testSave_CreateNewUser() {
        // Given: 一个新用户
        UserAuth newUser = new UserAuth();
        newUser.setUsername("new_test_user");
        newUser.setPasswordHash("$2a$10$test_hash");
        newUser.setStatus(UserStatus.ACTIVE.getCode());
        
        // When: 保存用户
        UserAuth saved = userAuthRepository.save(newUser);
        
        // Then: 应该保存成功并生成ID
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("new_test_user");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
    
    @Test
    void testFindById_WithRoles() {
        // Given: test_admin用户拥有角色
        UserAuth testUser = userAuthRepository.findByUsername("test_admin").orElseThrow();
        
        // When: 根据ID查询用户
        Optional<UserAuth> result = userAuthRepository.findById(testUser.getId());
        
        // Then: 应该级联查询到角色
        assertThat(result).isPresent();
        UserAuth user = result.get();
        assertThat(user.getRoles()).isNotEmpty();
    }
    
    @Test
    void testUpdate_ChangeStatus() {
        // Given: test_user用户
        UserAuth user = userAuthRepository.findByUsername("test_user").orElseThrow();
        String originalStatus = user.getStatus();
        
        // When: 更新状态为disabled
        user.setStatus(UserStatus.DISABLED.getCode());
        UserAuth updated = userAuthRepository.save(user);
        
        // Then: 应该更新成功
        assertThat(updated.getStatus()).isEqualTo(UserStatus.DISABLED.getCode());
        assertThat(updated.getStatus()).isNotEqualTo(originalStatus);
        
        // 清理：恢复原状态
        user.setStatus(originalStatus);
        userAuthRepository.save(user);
    }
    
    @Test
    void testFindByUsername_LoadsRoles() {
        // Given: test_admin用户拥有角色
        
        // When: 根据用户名查询（使用@EntityGraph加载角色）
        Optional<UserAuth> result = userAuthRepository.findByUsername("test_admin");
        
        // Then: 应该级联查询到角色
        assertThat(result).isPresent();
        UserAuth user = result.get();
        assertThat(user.getRoles()).isNotEmpty();
        assertThat(user.getPrimaryRoleCode()).isNotNull();
    }
}

