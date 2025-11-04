package com.open436.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发布帖子请求DTO
 */
@Data
@Schema(description = "发布帖子请求")
public class CreatePostDTO {
    
    @NotBlank(message = "标题不能为空")
    @Size(min = 5, max = 100, message = "标题长度必须在5-100个字符之间")
    @Schema(description = "帖子标题", example = "如何学习Spring Boot框架？", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
    
    @NotBlank(message = "内容不能为空")
    @Size(min = 10, max = 50000, message = "内容长度必须在10-50000个字符之间")
    @Schema(description = "帖子内容，支持富文本", example = "最近在学习Spring Boot，有哪些好的学习资源推荐？", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
    
    @NotNull(message = "板块ID不能为空")
    @Schema(description = "所属板块ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long boardId;
}

