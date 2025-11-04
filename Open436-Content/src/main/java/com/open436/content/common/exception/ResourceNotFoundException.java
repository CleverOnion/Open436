package com.open436.content.common.exception;

/**
 * 资源未找到异常
 */
public class ResourceNotFoundException extends BusinessException {
    
    private static final long serialVersionUID = 1L;
    
    public ResourceNotFoundException(String message) {
        super(404, message);
    }
    
    public ResourceNotFoundException(String resourceName, Long id) {
        super(404, String.format("%s [id=%d] 不存在", resourceName, id));
    }
}

