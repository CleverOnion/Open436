package com.open436.auth.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 错误码枚举
 * 规则: 第一位表示HTTP状态码类别，后续为具体错误编号
 */
@Getter
public enum ErrorCode {
    
    // ========== 4xx 客户端错误 ==========
    
    // 400 参数错误
    INVALID_PARAMETER(40000001, HttpStatus.BAD_REQUEST, "参数验证失败"),
    PASSWORD_MISMATCH(40001004, HttpStatus.BAD_REQUEST, "两次输入的密码不一致"),
    PASSWORD_SAME_AS_OLD(40001005, HttpStatus.BAD_REQUEST, "新密码不能与原密码相同"),
    
    // 401 认证错误
    INVALID_CREDENTIALS(40101001, HttpStatus.UNAUTHORIZED, "用户名或密码错误"),
    NOT_LOGGED_IN(40101002, HttpStatus.UNAUTHORIZED, "未登录，请先登录"),
    WRONG_OLD_PASSWORD(40101004, HttpStatus.UNAUTHORIZED, "原密码错误"),
    
    // 403 权限错误
    ACCOUNT_DISABLED(40301001, HttpStatus.FORBIDDEN, "账号已被禁用，请联系管理员"),
    INSUFFICIENT_PERMISSION(40301002, HttpStatus.FORBIDDEN, "权限不足"),
    INSUFFICIENT_ROLE(40301003, HttpStatus.FORBIDDEN, "需要管理员权限"),
    
    // 404 资源不存在
    RESOURCE_NOT_FOUND(40400000, HttpStatus.NOT_FOUND, "请求的资源不存在"),
    USER_NOT_FOUND(40401001, HttpStatus.NOT_FOUND, "用户不存在"),
    ROLE_NOT_FOUND(40401002, HttpStatus.NOT_FOUND, "角色不存在"),
    
    // 409 冲突
    USERNAME_EXISTS(40901001, HttpStatus.CONFLICT, "用户名已存在"),
    
    // ========== 5xx 服务器错误 ==========
    INTERNAL_SERVER_ERROR(50000000, HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误");
    
    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
    
    ErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}

