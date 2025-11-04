package com.open436.content.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 帖子列表项VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "帖子列表项")
public class PostListVO {
    
    @Schema(description = "帖子ID", example = "1001")
    private Long id;
    
    @Schema(description = "帖子标题", example = "如何学习Spring Boot框架？")
    private String title;
    
    @Schema(description = "内容预览（前200字）", example = "最近在学习Spring Boot，有哪些好的学习资源推荐？...")
    private String contentPreview;
    
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
    
    @Schema(description = "浏览量", example = "1250")
    private Long viewCount;
    
    @Schema(description = "回复数", example = "15")
    private Integer replyCount;
    
    @Schema(description = "点赞数", example = "28")
    private Integer likeCount;
    
    @Schema(description = "发布时间", example = "2025-11-03T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "最后编辑时间", example = "2025-11-03T15:45:00")
    private LocalDateTime lastEditedAt;
    
    @Schema(description = "是否已编辑", example = "false")
    private Boolean isEdited;
}

