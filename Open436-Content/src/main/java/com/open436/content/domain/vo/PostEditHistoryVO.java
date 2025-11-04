package com.open436.content.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 帖子编辑历史VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "帖子编辑历史")
public class PostEditHistoryVO {
    
    @Schema(description = "历史记录ID", example = "1")
    private Long id;
    
    @Schema(description = "帖子ID", example = "1001")
    private Long postId;
    
    @Schema(description = "版本号", example = "1")
    private Integer version;
    
    @Schema(description = "修改前的标题", example = "如何学习Spring Boot？")
    private String oldTitle;
    
    @Schema(description = "修改前的内容", example = "原始内容...")
    private String oldContent;
    
    @Schema(description = "修改前的板块ID", example = "1")
    private Long oldBoardId;
    
    @Schema(description = "修改前的板块名称", example = "Java技术")
    private String oldBoardName;
    
    @Schema(description = "编辑者用户ID", example = "1")
    private Long editedBy;
    
    @Schema(description = "编辑者昵称", example = "张三")
    private String editorName;
    
    @Schema(description = "编辑时间", example = "2025-11-03T15:45:00")
    private LocalDateTime editedAt;
    
    @Schema(description = "编辑原因", example = "修正错别字")
    private String editReason;
}

