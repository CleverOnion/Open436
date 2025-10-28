package com.open436.auth.dto;

import com.open436.auth.validation.PasswordMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改密码请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@PasswordMatch(message = "两次输入的密码不一致")
public class UpdatePasswordRequest {
    
    /**
     * 原密码
     */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;
    
    /**
     * 新密码（6-32字符）
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "新密码长度必须为6-32个字符")
    private String newPassword;
    
    /**
     * 确认新密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}


