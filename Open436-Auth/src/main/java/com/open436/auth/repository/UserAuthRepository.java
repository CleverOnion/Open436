package com.open436.auth.repository;

import com.open436.auth.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户实体（Optional）
     */
    Optional<UserAuth> findByUsername(String username);
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 根据状态查询用户列表
     * @param status 账号状态（active/disabled）
     * @return 用户列表
     */
    List<UserAuth> findByStatus(String status);
    
    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 新状态
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE UserAuth u SET u.status = :status WHERE u.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 根据用户名模糊查询
     * @param username 用户名关键字
     * @return 用户列表
     */
    List<UserAuth> findByUsernameContaining(String username);
}

