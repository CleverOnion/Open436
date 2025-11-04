package com.open436.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新帖子请求DTO
 */
@Data
@Schema(description = "更新帖子请求")
public class UpdatePostDTO {
    
    @Size(min = 5, max = 100, message = "标题长度必须在5-100个字符之间")
    @Schema(description = "帖子标题", example = "如何学习Spring Boot框架（修改版）")
    private String title;
    
    @Size(min = 10, max = 50000, message = "内容长度必须在10-50000个字符之间")
    @Schema(description = "帖子内容", example = "更新后的内容...")
    private String content;
    
    @Schema(description = "所属板块ID", example = "1")
    private Long boardId;
    
    @Schema(description = "编辑原因", example = "修正错别字")
    private String editReason;
}

