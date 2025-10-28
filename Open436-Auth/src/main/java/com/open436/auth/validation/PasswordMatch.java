package com.open436.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 密码匹配验证注解
 * 用于验证两次输入的密码是否一致
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchValidator.class)
@Documented
public @interface PasswordMatch {
    
    /**
     * 错误消息
     */
    String message() default "两次输入的密码不一致";
    
    /**
     * 新密码字段名
     */
    String newPasswordField() default "newPassword";
    
    /**
     * 确认密码字段名
     */
    String confirmPasswordField() default "confirmPassword";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}

