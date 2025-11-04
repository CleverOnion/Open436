package com.open436.content.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 帖子实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post", indexes = {
        @Index(name = "idx_post_author_id", columnList = "author_id"),
        @Index(name = "idx_post_board_id", columnList = "board_id"),
        @Index(name = "idx_post_created_at", columnList = "created_at"),
        @Index(name = "idx_post_is_deleted", columnList = "is_deleted"),
        @Index(name = "idx_post_pin_type", columnList = "pin_type"),
        @Index(name = "idx_post_board_created", columnList = "board_id,created_at")
})
@Comment("帖子主表")
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("帖子ID")
    private Long id;
    
    @Column(nullable = false, length = 100)
    @Comment("帖子标题")
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @Comment("帖子内容")
    private String content;
    
    @Column(name = "author_id", nullable = false)
    @Comment("作者用户ID")
    private Long authorId;
    
    @Column(name = "board_id", nullable = false)
    @Comment("所属板块ID")
    private Long boardId;
    
    // ========== 状态信息 ==========
    
    @Column(name = "is_deleted", nullable = false)
    @Comment("是否已删除")
    private Boolean isDeleted = false;
    
    @Column(name = "delete_reason", length = 500)
    @Comment("删除原因")
    private String deleteReason;
    
    @Column(name = "deleted_by")
    @Comment("删除操作者ID")
    private Long deletedBy;
    
    @Column(name = "deleted_at")
    @Comment("删除时间")
    private LocalDateTime deletedAt;
    
    @Column(name = "pin_type", nullable = false)
    @Comment("置顶类型：0-不置顶，1-板块置顶，2-全局置顶")
    private Integer pinType = 0;
    
    @Column(name = "pinned_at")
    @Comment("置顶时间")
    private LocalDateTime pinnedAt;
    
    @Column(name = "pinned_by")
    @Comment("置顶操作者ID")
    private Long pinnedBy;
    
    // ========== 统计信息 ==========
    
    @Column(name = "view_count", nullable = false)
    @Comment("浏览量")
    private Long viewCount = 0L;
    
    @Column(name = "reply_count", nullable = false)
    @Comment("回复数")
    private Integer replyCount = 0;
    
    @Column(name = "like_count", nullable = false)
    @Comment("点赞数")
    private Integer likeCount = 0;
    
    // ========== 编辑信息 ==========
    
    @Column(name = "edit_count", nullable = false)
    @Comment("编辑次数")
    private Integer editCount = 0;
    
    @Column(name = "last_edited_at")
    @Comment("最后编辑时间")
    private LocalDateTime lastEditedAt;
    
    @Column(name = "last_edited_by")
    @Comment("最后编辑者ID")
    private Long lastEditedBy;
    
    // ========== 时间戳 ==========
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("创建时间")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @Comment("更新时间")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

