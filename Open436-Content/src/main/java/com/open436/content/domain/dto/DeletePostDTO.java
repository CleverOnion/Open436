package com.open436.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 删除帖子请求DTO（管理员使用）
 */
@Data
@Schema(description = "删除帖子请求")
public class DeletePostDTO {
    
    @NotBlank(message = "删除原因不能为空")
    @Schema(description = "删除原因", example = "违反社区规定", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;
    
    @Schema(description = "是否硬删除（永久删除），默认为软删除", example = "false")
    private Boolean permanent = false;
}

