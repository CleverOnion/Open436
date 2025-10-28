package com.open436.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户状态请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {
    
    /**
     * 状态（active/disabled）
     */
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(active|disabled)$", message = "状态只能是 active 或 disabled")
    private String status;
}


