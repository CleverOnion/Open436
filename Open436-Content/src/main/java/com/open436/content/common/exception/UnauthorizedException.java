package com.open436.content.common.exception;

/**
 * 未授权异常
 * 当用户未登录或认证失败时抛出
 */
public class UnauthorizedException extends BusinessException {
    
    public UnauthorizedException(String message) {
        super(401, message);
    }
    
    public UnauthorizedException() {
        super(401, "未登录或登录已过期，请先登录");
    }
}

