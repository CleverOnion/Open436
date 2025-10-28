package com.open436.auth.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.open436.auth.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GlobalExceptionHandler 测试
 * 测试全局异常处理器
 */
class GlobalExceptionHandlerTest {
    
    private GlobalExceptionHandler exceptionHandler;
    
    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }
    
    @Test
    void testHandleNotLoginException() {
        // Given: 未登录异常
        NotLoginException exception = new NotLoginException("未登录", "token", "default");
        
        // When: 处理异常
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleNotLoginException(exception);
        
        // Then: 应该返回401
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(40101002);
        assertThat(response.getBody().getMessage()).contains("未登录");
    }
    
    @Test
    void testHandleNotPermissionException() {
        // Given: 权限不足异常
        NotPermissionException exception = new NotPermissionException("post:create");
        
        // When: 处理异常
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleNotPermissionException(exception);
        
        // Then: 应该返回403
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(40301002);
        assertThat(response.getBody().getMessage()).isEqualTo("权限不足");
    }
    
    @Test
    void testHandleNotRoleException() {
        // Given: 角色不足异常
        NotRoleException exception = new NotRoleException("admin");
        
        // When: 处理异常
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleNotRoleException(exception);
        
        // Then: 应该返回403
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(40301003);
        assertThat(response.getBody().getMessage()).isEqualTo("需要管理员权限");
    }
    
    @Test
    void testHandleBusinessException() {
        // Given: 业务异常
        BusinessException exception = new BusinessException(40401001, "用户不存在");
        
        // When: 处理异常
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(exception);
        
        // Then: 应该返回相应的状态码
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(40401001);
        assertThat(response.getBody().getMessage()).isEqualTo("用户不存在");
    }
    
    @Test
    void testHandleBusinessException_Unauthorized() {
        // Given: 业务异常 - 401错误
        BusinessException exception = new BusinessException(40101001, "用户名或密码错误");
        
        // When: 处理异常
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(exception);
        
        // Then: 应该返回401
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(40101001);
    }
    
    @Test
    void testHandleValidationException() {
        // Given: 参数验证异常
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "loginRequest");
        bindingResult.addError(new FieldError("loginRequest", "username", "用户名不能为空"));
        bindingResult.addError(new FieldError("loginRequest", "password", "密码长度必须为6-32个字符"));
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);
        
        // When: 处理异常
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleValidationException(exception);
        
        // Then: 应该返回400
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(40000001);
        assertThat(response.getBody().getMessage()).contains("参数验证失败");
        assertThat(response.getBody().getMessage()).contains("用户名不能为空");
    }
    
    @Test
    void testHandleNoResourceFoundException() {
        // Given: 资源不存在异常
        NoResourceFoundException exception = new NoResourceFoundException(null, "/api/nonexistent");
        
        // When: 处理异常
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleNoResourceFoundException(exception);
        
        // Then: 应该返回404
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(40400000);
        assertThat(response.getBody().getMessage()).isEqualTo("请求的资源不存在");
    }
    
    @Test
    void testHandleGenericException() {
        // Given: 未知异常
        Exception exception = new RuntimeException("系统内部错误");
        
        // When: 处理异常
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleException(exception);
        
        // Then: 应该返回500
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(50000000);
        assertThat(response.getBody().getMessage()).isEqualTo("服务器内部错误");
    }
    
    @Test
    void testHandleNullPointerException() {
        // Given: 空指针异常
        Exception exception = new NullPointerException("空指针");
        
        // When: 处理异常
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleException(exception);
        
        // Then: 应该返回500
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(50000000);
        assertThat(response.getBody().getMessage()).isEqualTo("服务器内部错误");
    }
}

