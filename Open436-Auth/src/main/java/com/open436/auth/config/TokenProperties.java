package com.open436.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Token配置属性类
 * 从application.yml中读取sa-token配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "sa-token")
public class TokenProperties {
    
    /**
     * Token名称
     */
    private String tokenName = "token";
    
    /**
     * Token超时时间（秒）
     */
    private Long timeout = 2592000L;
    
    /**
     * Token风格
     */
    private String tokenStyle = "uuid";
    
    /**
     * 是否允许同一账号并发登录
     */
    private Boolean isConcurrent = true;
    
    /**
     * 是否共享Token
     */
    private Boolean isShare = true;
    
    /**
     * 是否自动续签
     */
    private Boolean autoRenew = true;
    
    /**
     * 活动超时时间（-1表示不限制）
     */
    private Long activeTimeout = -1L;
    
    /**
     * 是否输出日志
     */
    private Boolean isLog = false;
}

