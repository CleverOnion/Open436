"""
Custom permissions
"""
from rest_framework.permissions import BasePermission


class IsAdminUser(BasePermission):
    """
    仅允许管理员访问
    
    依赖于UserInfoMiddleware从headers注入的用户信息
    """
    
    message = '需要管理员权限'
    
    def has_permission(self, request, view):
        """
        检查用户是否为管理员
        """
        # 检查是否认证
        if not getattr(request, 'is_authenticated', False):
            return False
        
        # 检查是否为管理员
        return getattr(request, 'is_admin', False)




