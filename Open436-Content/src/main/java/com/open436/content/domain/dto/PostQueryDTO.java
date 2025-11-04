package com.open436.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 帖子查询条件DTO
 */
@Data
@Schema(description = "帖子查询条件")
public class PostQueryDTO {
    
    @Schema(description = "板块ID，不传则查询所有板块", example = "1")
    private Long boardId;
    
    @Schema(description = "作者用户ID", example = "1001")
    private Long authorId;
    
    @Schema(description = "排序方式：latest-最新发布（默认），reply-最新回复，hot-热度", example = "latest")
    private String sortBy = "latest";
    
    @Schema(description = "是否只查询置顶帖子", example = "false")
    private Boolean pinnedOnly = false;
    
    @Schema(description = "是否包含已删除的帖子（仅管理员）", example = "false")
    private Boolean includeDeleted = false;
    
    @Schema(description = "页码，从1开始", example = "1")
    private Integer page = 1;
    
    @Schema(description = "每页大小，最大50", example = "20")
    private Integer pageSize = 20;
}

