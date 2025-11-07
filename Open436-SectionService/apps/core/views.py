"""
Core views for health check and monitoring
"""
from django.http import JsonResponse
from django.db import connection


def health_check(request):
    """
    健康检查端点
    
    GET /health/
    """
    try:
        # 检查数据库连接
        with connection.cursor() as cursor:
            cursor.execute('SELECT 1')
        
        return JsonResponse({
            'status': 'healthy',
            'service': 'section-service',
            'version': '1.0.0'
        })
    except Exception as e:
        return JsonResponse({
            'status': 'unhealthy',
            'service': 'section-service',
            'error': str(e)
        }, status=500)






