package com.open436.content.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 帖子详情VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "帖子详情")
public class PostDetailVO {
    
    @Schema(description = "帖子ID", example = "1001")
    private Long id;
    
    @Schema(description = "帖子标题", example = "如何学习Spring Boot框架？")
    private String title;
    
    @Schema(description = "帖子完整内容", example = "最近在学习Spring Boot，有哪些好的学习资源推荐？...")
    private String content;
    
    @Schema(description = "作者用户ID", example = "1")
    private Long authorId;
    
    @Schema(description = "作者昵称", example = "张三")
    private String authorName;
    
    @Schema(description = "作者头像URL", example = "https://example.com/avatar.jpg")
    private String authorAvatar;
    
    @Schema(description = "所属板块ID", example = "1")
    private Long boardId;
    
    @Schema(description = "所属板块名称", example = "Java技术")
    private String boardName;
    
    @Schema(description = "置顶类型：0-不置顶，1-板块置顶，2-全局置顶", example = "0")
    private Integer pinType;
    
    @Schema(description = "是否已删除", example = "false")
    private Boolean isDeleted;
    
    @Schema(description = "删除原因", example = "违反社区规定")
    private String deleteReason;
    
    @Schema(description = "浏览量", example = "1250")
    private Long viewCount;
    
    @Schema(description = "回复数", example = "15")
    private Integer replyCount;
    
    @Schema(description = "点赞数", example = "28")
    private Integer likeCount;
    
    @Schema(description = "编辑次数", example = "2")
    private Integer editCount;
    
    @Schema(description = "发布时间", example = "2025-11-03T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间", example = "2025-11-03T15:45:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "最后编辑时间", example = "2025-11-03T15:45:00")
    private LocalDateTime lastEditedAt;
    
    @Schema(description = "最后编辑者ID", example = "1")
    private Long lastEditedBy;
    
    @Schema(description = "是否已编辑", example = "true")
    private Boolean isEdited;
}

