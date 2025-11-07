"""
Custom exception handler
"""
from rest_framework.views import exception_handler
from rest_framework import status
from .responses import error_response
import logging

logger = logging.getLogger(__name__)


def custom_exception_handler(exc, context):
    """
    自定义异常处理器
    
    统一异常响应格式
    """
    # 调用DRF的默认异常处理器
    response = exception_handler(exc, context)
    
    if response is not None:
        # 重新格式化响应
        error_message = response.data.get('detail', str(exc))
        error_type = exc.__class__.__name__
        
        # 处理验证错误
        if hasattr(exc, 'detail') and isinstance(exc.detail, dict):
            return error_response(
                message='参数验证失败',
                error='ValidationError',
                status_code=response.status_code,
                details=response.data
            )
        
        return error_response(
            message=error_message,
            error=error_type,
            status_code=response.status_code
        )
    
    # 未捕获的异常
    logger.error(f'Unhandled exception: {exc}', exc_info=True)
    return error_response(
        message='服务器内部错误',
        error='InternalError',
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR
    )






