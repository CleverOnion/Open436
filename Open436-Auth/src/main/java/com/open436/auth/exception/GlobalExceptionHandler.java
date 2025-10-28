package com.open436.auth.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.open436.auth.dto.ApiResponse;
import com.open436.auth.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理所有异常并返回标准格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Sa-Token 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotLoginException(NotLoginException e) {
        log.warn("未登录异常: {}", e.getMessage());
        ErrorCode errorCode = ErrorCode.NOT_LOGGED_IN;
        
        return ResponseEntity.status(errorCode.getHttpStatus())
            .body(ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(System.currentTimeMillis())
                .build());
    }
    
    /**
     * Sa-Token 权限不足异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotPermissionException(NotPermissionException e) {
        log.warn("权限不足异常: {}", e.getMessage());
        ErrorCode errorCode = ErrorCode.INSUFFICIENT_PERMISSION;
        
        return ResponseEntity.status(errorCode.getHttpStatus())
            .body(ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(System.currentTimeMillis())
                .build());
    }
    
    /**
     * Sa-Token 角色不足异常
     */
    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotRoleException(NotRoleException e) {
        log.warn("角色不足异常: {}", e.getMessage());
        ErrorCode errorCode = ErrorCode.INSUFFICIENT_ROLE;
        
        return ResponseEntity.status(errorCode.getHttpStatus())
            .body(ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(System.currentTimeMillis())
                .build());
    }
    
    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        
        return ResponseEntity.status(e.getHttpStatus())
            .body(ApiResponse.<Void>builder()
                .code(e.getCode())
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .build());
    }
    
    /**
     * 参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        log.warn("参数验证失败: {}", message);
        ErrorCode errorCode = ErrorCode.INVALID_PARAMETER;
        
        return ResponseEntity.status(errorCode.getHttpStatus())
            .body(ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message("参数验证失败: " + message)
                .timestamp(System.currentTimeMillis())
                .build());
    }
    
    /**
     * 资源不存在异常（静默处理，不打印错误日志）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        // 不打印日志，静默返回404
        ErrorCode errorCode = ErrorCode.RESOURCE_NOT_FOUND;
        
        return ResponseEntity.status(errorCode.getHttpStatus())
            .body(ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(System.currentTimeMillis())
                .build());
    }
    
    /**
     * 其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("系统异常: ", e);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        
        return ResponseEntity.status(errorCode.getHttpStatus())
            .body(ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(System.currentTimeMillis())
                .build());
    }
}


