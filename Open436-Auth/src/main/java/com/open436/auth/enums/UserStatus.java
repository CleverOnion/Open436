package com.open436.auth.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatus {
    
    /**
     * 正常
     */
    ACTIVE("active", "正常"),
    
    /**
     * 禁用
     */
    DISABLED("disabled", "禁用");
    
    private final String code;
    private final String description;
    
    UserStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据代码获取枚举
     * @param code 状态代码
     * @return UserStatus枚举
     */
    public static UserStatus fromCode(String code) {
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的用户状态: " + code);
    }
    
    /**
     * 判断是否为有效状态代码
     * @param code 状态代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}

