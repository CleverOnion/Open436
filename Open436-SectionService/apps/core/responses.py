"""
Standardized API response helpers
"""
from rest_framework.response import Response
from rest_framework import status
import time


def success_response(data=None, message='success', status_code=status.HTTP_200_OK):
    """
    成功响应
    
    Args:
        data: 响应数据
        message: 响应消息
        status_code: HTTP状态码
    
    Returns:
        Response对象
    """
    return Response({
        'code': status_code,
        'message': message,
        'data': data,
        'timestamp': int(time.time() * 1000)
    }, status=status_code)


def error_response(message='error', error='Error', status_code=status.HTTP_400_BAD_REQUEST, details=None):
    """
    错误响应
    
    Args:
        message: 错误消息
        error: 错误类型
        status_code: HTTP状态码
        details: 详细错误信息
    
    Returns:
        Response对象
    """
    response_data = {
        'code': status_code,
        'message': message,
        'error': error,
        'timestamp': int(time.time() * 1000)
    }
    
    if details:
        response_data['details'] = details
    
    return Response(response_data, status=status_code)




