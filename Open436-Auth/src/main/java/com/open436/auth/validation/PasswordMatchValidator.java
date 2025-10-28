package com.open436.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

/**
 * 密码匹配验证器
 */
public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {
    
    private String newPasswordField;
    private String confirmPasswordField;
    
    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        this.newPasswordField = constraintAnnotation.newPasswordField();
        this.confirmPasswordField = constraintAnnotation.confirmPasswordField();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        try {
            // 通过反射获取字段值
            Field newPasswordFieldObj = value.getClass().getDeclaredField(newPasswordField);
            Field confirmPasswordFieldObj = value.getClass().getDeclaredField(confirmPasswordField);
            
            newPasswordFieldObj.setAccessible(true);
            confirmPasswordFieldObj.setAccessible(true);
            
            Object newPasswordValue = newPasswordFieldObj.get(value);
            Object confirmPasswordValue = confirmPasswordFieldObj.get(value);
            
            // 如果两个字段都为null，则认为匹配
            if (newPasswordValue == null && confirmPasswordValue == null) {
                return true;
            }
            
            // 比较两个密码是否相等
            return newPasswordValue != null && newPasswordValue.equals(confirmPasswordValue);
            
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("密码匹配验证失败: " + e.getMessage(), e);
        }
    }
}

