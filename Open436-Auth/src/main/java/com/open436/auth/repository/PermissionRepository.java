package com.open436.auth.repository;

import com.open436.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限 Repository
 * 提供权限相关的数据访问方法
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    /**
     * 根据权限代码查询权限
     * @param code 权限代码（如：post:create）
     * @return 权限实体（Optional）
     */
    Optional<Permission> findByCode(String code);
    
    /**
     * 根据资源类型查询权限列表
     * @param resource 资源类型（如：post, user）
     * @return 权限列表
     */
    List<Permission> findByResource(String resource);
    
    /**
     * 检查权限代码是否存在
     * @param code 权限代码
     * @return 是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 根据用户ID查询该用户的所有权限
     * 通过 用户 -> 角色 -> 权限 的关联查询
     * @param userId 用户ID
     * @return 权限列表
     */
    @Query(value = "SELECT DISTINCT p.* FROM permissions p " +
           "INNER JOIN role_permissions rp ON p.id = rp.permission_id " +
           "INNER JOIN user_roles ur ON rp.role_id = ur.role_id " +
           "WHERE ur.user_id = :userId", 
           nativeQuery = true)
    List<Permission> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID查询该角色的所有权限
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Query(value = "SELECT p.* FROM permissions p " +
           "INNER JOIN role_permissions rp ON p.id = rp.permission_id " +
           "WHERE rp.role_id = :roleId", 
           nativeQuery = true)
    List<Permission> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据资源和操作查询权限
     * @param resource 资源类型
     * @param action 操作类型
     * @return 权限实体（Optional）
     */
    Optional<Permission> findByResourceAndAction(String resource, String action);
}

