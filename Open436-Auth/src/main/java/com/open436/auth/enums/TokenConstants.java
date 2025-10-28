package com.open436.auth.enums;

/**
 * Token相关常量
 */
public final class TokenConstants {
    
    private TokenConstants() {
        // 防止实例化
    }
    
    /**
     * Token超时时间（秒）
     * 30天 = 30 * 24 * 60 * 60 = 2592000秒
     */
    public static final long TOKEN_TIMEOUT = 2592000L;
    
    /**
     * Session键名 - 用户名
     */
    public static final String SESSION_KEY_USERNAME = "username";
    
    /**
     * Session键名 - 角色
     */
    public static final String SESSION_KEY_ROLE = "role";
    
    /**
     * 设备类型 - Web
     */
    public static final String DEVICE_WEB = "web";
    
    /**
     * 默认角色
     */
    public static final String DEFAULT_ROLE = "user";
}

