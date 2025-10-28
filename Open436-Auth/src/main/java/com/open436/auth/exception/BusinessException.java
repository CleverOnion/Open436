package com.open436.auth.exception;

import com.open436.auth.enums.ErrorCode;
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
     * 构造函数（使用ErrorCode枚举）
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }
    
    /**
     * 构造函数（使用ErrorCode枚举，自定义消息）
     * @param errorCode 错误码枚举
     * @param customMessage 自定义错误消息
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }
    
    /**
     * 构造函数
     * @param code 错误码
     * @param message 错误消息
     * @deprecated 推荐使用 {@link #BusinessException(ErrorCode)}
     */
    @Deprecated
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
     * @deprecated 推荐使用 {@link #BusinessException(ErrorCode)}
     */
    @Deprecated
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

