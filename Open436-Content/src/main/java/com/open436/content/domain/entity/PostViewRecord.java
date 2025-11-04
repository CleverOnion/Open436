package com.open436.content.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 帖子浏览记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post_view_record", indexes = {
        @Index(name = "idx_post_view_record_post_id", columnList = "post_id"),
        @Index(name = "idx_post_view_record_user_id", columnList = "user_id"),
        @Index(name = "idx_post_view_record_viewed_at", columnList = "viewed_at"),
        @Index(name = "idx_post_view_record_post_user_time", columnList = "post_id,user_id,viewed_at"),
        @Index(name = "idx_post_view_record_post_ip_time", columnList = "post_id,ip_address,viewed_at")
})
@Comment("帖子浏览记录表")
public class PostViewRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("记录ID")
    private Long id;
    
    @Column(name = "post_id", nullable = false)
    @Comment("帖子ID")
    private Long postId;
    
    @Column(name = "user_id")
    @Comment("用户ID（登录用户）")
    private Long userId;
    
    @Column(name = "ip_address", length = 45)
    @Comment("IP地址（未登录用户）")
    private String ipAddress;
    
    @Column(name = "viewed_at", nullable = false)
    @Comment("浏览时间")
    private LocalDateTime viewedAt;
    
    @PrePersist
    protected void onCreate() {
        viewedAt = LocalDateTime.now();
    }
}

