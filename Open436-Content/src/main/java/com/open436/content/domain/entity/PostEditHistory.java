package com.open436.content.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 帖子编辑历史实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post_edit_history", indexes = {
        @Index(name = "idx_post_edit_history_post_id", columnList = "post_id"),
        @Index(name = "idx_post_edit_history_edited_at", columnList = "edited_at")
})
@Comment("帖子编辑历史表")
public class PostEditHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("历史记录ID")
    private Long id;
    
    @Column(name = "post_id", nullable = false)
    @Comment("帖子ID")
    private Long postId;
    
    @Column(nullable = false)
    @Comment("版本号")
    private Integer version;
    
    // ========== 历史内容 ==========
    
    @Column(name = "old_title", nullable = false, length = 100)
    @Comment("修改前的标题")
    private String oldTitle;
    
    @Column(name = "old_content", nullable = false, columnDefinition = "TEXT")
    @Comment("修改前的内容")
    private String oldContent;
    
    @Column(name = "old_board_id")
    @Comment("修改前的板块ID")
    private Long oldBoardId;
    
    // ========== 编辑信息 ==========
    
    @Column(name = "edited_by", nullable = false)
    @Comment("编辑者用户ID")
    private Long editedBy;
    
    @Column(name = "edited_at", nullable = false)
    @Comment("编辑时间")
    private LocalDateTime editedAt;
    
    @Column(name = "edit_reason", length = 500)
    @Comment("编辑原因")
    private String editReason;
    
    @PrePersist
    protected void onCreate() {
        editedAt = LocalDateTime.now();
    }
}

