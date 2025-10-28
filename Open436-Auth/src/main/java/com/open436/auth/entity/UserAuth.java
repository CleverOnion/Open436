package com.open436.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 用户认证表
 * 存储用户登录凭证和账号状态
 */
@Entity
@Table(name = "users_auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"roles"})
@EntityListeners(AuditingEntityListener.class)
public class UserAuth {
    
    /**
     * 用户ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户名（唯一，3-20字符）
     */
    @Column(unique = true, nullable = false, length = 20)
    private String username;
    
    /**
     * 密码哈希（BCrypt加密）
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    /**
     * 账号状态：active-正常, disabled-禁用
     */
    @Column(nullable = false, length = 20)
    private String status = "active";
    
    /**
     * 最后登录时间
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 用户角色（多对多关系）
     * 改为LAZY加载，避免N+1查询问题
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    /**
     * 获取用户的主要角色（第一个角色）
     * @return 角色代码，如果没有角色则返回 "user"
     */
    public String getPrimaryRoleCode() {
        return roles.stream()
            .findFirst()
            .map(Role::getCode)
            .orElse("user");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAuth userAuth = (UserAuth) o;
        return Objects.equals(id, userAuth.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

