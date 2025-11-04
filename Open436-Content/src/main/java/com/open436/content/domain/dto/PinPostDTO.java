package com.open436.content.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 置顶帖子请求DTO
 */
@Data
@Schema(description = "置顶帖子请求")
public class PinPostDTO {
    
    @NotNull(message = "置顶类型不能为空")
    @Min(value = 1, message = "置顶类型必须为1（板块置顶）或2（全局置顶）")
    @Max(value = 2, message = "置顶类型必须为1（板块置顶）或2（全局置顶）")
    @Schema(description = "置顶类型：1-板块置顶，2-全局置顶", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pinType;
}

