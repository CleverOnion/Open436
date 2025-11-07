"""
Internal URLs for M3 content service
"""
from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views_internal import InternalSectionViewSet

router = DefaultRouter()
router.register(r'sections', InternalSectionViewSet, basename='internal-section')

urlpatterns = [
    path('', include(router.urls)),
]

