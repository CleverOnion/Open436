package com.open436.auth.repository;

import com.open436.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色 Repository
 * 提供角色相关的数据访问方法
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * 根据角色代码查询角色
     * @param code 角色代码（如：user, admin）
     * @return 角色实体（Optional）
     */
    Optional<Role> findByCode(String code);
    
    /**
     * 根据角色名称查询角色
     * @param name 角色名称
     * @return 角色实体（Optional）
     */
    Optional<Role> findByName(String name);
    
    /**
     * 检查角色代码是否存在
     * @param code 角色代码
     * @return 是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 根据用户ID查询该用户的所有角色
     * @param userId 用户ID
     * @return 角色列表
     */
    @Query(value = "SELECT r.* FROM roles r " +
           "INNER JOIN user_roles ur ON r.id = ur.role_id " +
           "WHERE ur.user_id = :userId", 
           nativeQuery = true)
    List<Role> findByUserId(@Param("userId") Long userId);
}

