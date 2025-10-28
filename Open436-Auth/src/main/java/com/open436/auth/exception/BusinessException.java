package com.open436.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * HTTP 状态码
     */
    private final HttpStatus httpStatus;
    
    /**
     * 构造函数
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.httpStatus = mapToHttpStatus(code);
    }
    
    /**
     * 构造函数（带 HTTP 状态码）
     * @param code 错误码
     * @param message 错误消息
     * @param httpStatus HTTP 状态码
     */
    public BusinessException(Integer code, String message, HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }
    
    /**
     * 根据错误码映射 HTTP 状态码
     * @param code 错误码
     * @return HTTP 状态码
     */
    private static HttpStatus mapToHttpStatus(Integer code) {
        if (code >= 40000000 && code < 40100000) {
            return HttpStatus.BAD_REQUEST;
        } else if (code >= 40100000 && code < 40300000) {
            return HttpStatus.UNAUTHORIZED;
        } else if (code >= 40300000 && code < 40400000) {
            return HttpStatus.FORBIDDEN;
        } else if (code >= 40400000 && code < 40500000) {
            return HttpStatus.NOT_FOUND;
        } else if (code >= 40900000 && code < 41000000) {
            return HttpStatus.CONFLICT;
        } else if (code >= 42900000 && code < 43000000) {
            return HttpStatus.TOO_MANY_REQUESTS;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}

