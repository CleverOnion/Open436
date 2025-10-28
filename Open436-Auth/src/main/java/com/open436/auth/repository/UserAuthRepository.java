package com.open436.auth.repository;

import com.open436.auth.entity.UserAuth;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户认证 Repository
 * 提供用户认证相关的数据访问方法
 */
@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    
    /**
     * 根据用户名查询用户（带角色信息）
     * 使用@EntityGraph避免N+1查询问题
     * @param username 用户名
     * @return 用户实体（Optional）
     */
    @EntityGraph(attributePaths = {"roles"})
    Optional<UserAuth> findByUsername(String username);
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 根据状态查询用户列表（带角色信息）
     * @param status 账号状态（active/disabled）
     * @return 用户列表
     */
    @EntityGraph(attributePaths = {"roles"})
    List<UserAuth> findByStatus(String status);
}

