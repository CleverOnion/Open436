package com.open436.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 权限表
 * 定义系统权限
 */
@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Permission {
    
    /**
     * 权限ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 权限名称（中文）
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 权限代码（唯一标识）
     * 格式: {resource}:{action}
     * 示例: post:create, user:manage
     */
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    /**
     * 资源类型（如：post, user, section）
     */
    @Column(nullable = false, length = 50)
    private String resource;
    
    /**
     * 操作类型（create, read, update, delete, manage）
     */
    @Column(nullable = false, length = 20)
    private String action;
    
    /**
     * 权限描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

