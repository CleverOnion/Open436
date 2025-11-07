"""
Section URLs
"""
from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import SectionViewSet

# 创建路由器
router = DefaultRouter()
router.register(r'sections', SectionViewSet, basename='section')

urlpatterns = [
    path('', include(router.urls)),
]

