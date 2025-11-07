"""
Section views
"""
from rest_framework import viewsets, status
from rest_framework.permissions import AllowAny
from rest_framework.decorators import action
from django.shortcuts import get_object_or_404
from django.db import transaction
from django.db.models import Sum

from .models import Section
from .serializers import (
    SectionListSerializer,
    SectionDetailSerializer,
    SectionCreateSerializer,
    SectionUpdateSerializer,
    SectionStatusSerializer,
    SectionReorderSerializer,
    SectionStatisticsSerializer,
)
from apps.core.permissions import IsAdminUser
from apps.core.responses import success_response, error_response


class SectionViewSet(viewsets.ModelViewSet):
    """
    板块视图集
    
    提供板块的 CRUD 操作和管理功能
    """
    
    queryset = Section.objects.all()
    
    def get_queryset(self):
        """
        根据用户权限过滤查询集
        """
        queryset = super().get_queryset()
        
        # 普通用户只能看到启用的板块
        if not getattr(self.request, 'is_admin', False):
            queryset = queryset.filter(is_enabled=True)
        
        return queryset.order_by('sort_order', 'id')
    
    def get_serializer_class(self):
        """
        根据操作选择序列化器
        """
        if self.action == 'list':
            return SectionListSerializer
        elif self.action == 'retrieve':
            return SectionDetailSerializer
        elif self.action == 'create':
            return SectionCreateSerializer
        elif self.action in ['update', 'partial_update']:
            return SectionUpdateSerializer
        elif self.action == 'status':
            return SectionStatusSerializer
        elif self.action == 'reorder':
            return SectionReorderSerializer
        elif self.action == 'statistics':
            return SectionStatisticsSerializer
        else:
            return SectionDetailSerializer
    
    def get_permissions(self):
        """
        根据操作设置权限
        """
        if self.action in ['list', 'retrieve']:
            # 公开接口
            return [AllowAny()]
        else:
            # 管理员接口（阶段4实现）
            return [IsAdminUser()]
    
    def retrieve(self, request, *args, **kwargs):
        """
        获取板块详情（支持 ID 或 slug）
        """
        lookup_value = kwargs.get('pk')
        
        # 尝试通过 ID 查询
        if lookup_value.isdigit():
            instance = get_object_or_404(Section, id=lookup_value)
        else:
            # 通过 slug 查询
            instance = get_object_or_404(Section, slug=lookup_value)
        
        # 如果不是管理员，只能查看启用的板块
        if not getattr(request, 'is_admin', False) and not instance.is_enabled:
            return error_response(
                message='板块不存在',
                error='NotFound',
                status_code=status.HTTP_404_NOT_FOUND
            )
        
        serializer = self.get_serializer(instance)
        return success_response(data=serializer.data)
    
    def list(self, request, *args, **kwargs):
        """
        获取板块列表
        """
        queryset = self.get_queryset()
        page = self.paginate_queryset(queryset)
        
        if page is not None:
            serializer = self.get_serializer(page, many=True)
            return self.get_paginated_response(serializer.data)
        
        serializer = self.get_serializer(queryset, many=True)
        return success_response(data=serializer.data)
    
    def create(self, request, *args, **kwargs):
        """
        创建板块（管理员）
        """
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        
        # 创建板块
        instance = serializer.save()
        
        # 返回详情
        detail_serializer = SectionDetailSerializer(instance)
        return success_response(
            data=detail_serializer.data,
            message='板块创建成功',
            status_code=status.HTTP_201_CREATED
        )
    
    def update(self, request, *args, **kwargs):
        """
        更新板块（管理员）
        """
        partial = kwargs.pop('partial', False)
        instance = self.get_object()
        
        serializer = self.get_serializer(instance, data=request.data, partial=partial)
        serializer.is_valid(raise_exception=True)
        serializer.save()
        
        # 返回更新后的详情
        detail_serializer = SectionDetailSerializer(instance)
        return success_response(
            data=detail_serializer.data,
            message='板块更新成功'
        )
    
    def destroy(self, request, *args, **kwargs):
        """
        删除板块（管理员，软删除）
        """
        instance = self.get_object()
        
        # 检查是否可删除
        can_delete, reason = instance.can_be_deleted()
        if not can_delete:
            return error_response(
                message=reason,
                error='CannotDelete',
                status_code=status.HTTP_400_BAD_REQUEST
            )
        
        # 执行软删除（禁用）
        try:
            instance.disable()
            return success_response(message='板块已删除（禁用）')
        except ValueError as e:
            return error_response(
                message=str(e),
                error='CannotDelete',
                status_code=status.HTTP_400_BAD_REQUEST
            )
    
    @action(detail=True, methods=['put'], url_path='status')
    def status(self, request, pk=None):
        """
        启用/禁用板块（管理员）
        
        PUT /api/sections/{id}/status/
        Body: {"is_enabled": true/false}
        """
        instance = self.get_object()
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        
        is_enabled = serializer.validated_data['is_enabled']
        
        try:
            if is_enabled:
                instance.enable()
                message = '板块已启用'
            else:
                instance.disable()
                message = '板块已禁用'
            
            detail_serializer = SectionDetailSerializer(instance)
            return success_response(
                data=detail_serializer.data,
                message=message
            )
        except ValueError as e:
            return error_response(
                message=str(e),
                error='OperationFailed',
                status_code=status.HTTP_400_BAD_REQUEST
            )
    
    @action(detail=False, methods=['put'], url_path='reorder')
    def reorder(self, request):
        """
        批量调整板块排序（管理员）
        
        PUT /api/sections/reorder/
        Body: {
            "sections": [
                {"id": 1, "sort_order": 10},
                {"id": 2, "sort_order": 20}
            ]
        }
        """
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        
        sections_data = serializer.validated_data['sections']
        
        # 批量更新
        try:
            with transaction.atomic():
                for item in sections_data:
                    Section.objects.filter(id=item['id']).update(
                        sort_order=item['sort_order']
                    )
            
            return success_response(message=f'成功调整 {len(sections_data)} 个板块的排序')
        except Exception as e:
            return error_response(
                message=f'排序更新失败: {str(e)}',
                error='UpdateFailed',
                status_code=status.HTTP_400_BAD_REQUEST
            )
    
    @action(detail=False, methods=['get'], url_path='statistics')
    def statistics(self, request):
        """
        获取板块统计数据（管理员）
        
        GET /api/sections/statistics/
        """
        # 统计数据
        total_sections = Section.objects.count()
        enabled_sections = Section.objects.filter(is_enabled=True).count()
        disabled_sections = total_sections - enabled_sections
        total_posts = Section.objects.aggregate(Sum('posts_count'))['posts_count__sum'] or 0
        
        # 各板块详情
        sections = Section.objects.all().order_by('sort_order')
        sections_data = []
        for section in sections:
            sections_data.append({
                'id': section.id,
                'slug': section.slug,
                'name': section.name,
                'is_enabled': section.is_enabled,
                'posts_count': section.posts_count,
                'sort_order': section.sort_order
            })
        
        data = {
            'total_sections': total_sections,
            'enabled_sections': enabled_sections,
            'disabled_sections': disabled_sections,
            'total_posts': total_posts,
            'sections': sections_data
        }
        
        return success_response(data=data)

