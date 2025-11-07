"""
Django admin configuration for sections
"""
from django.contrib import admin
from .models import Section


@admin.register(Section)
class SectionAdmin(admin.ModelAdmin):
    """板块管理后台配置"""
    
    list_display = ['id', 'name', 'slug', 'color', 'sort_order', 'is_enabled', 'posts_count', 'created_at']
    list_filter = ['is_enabled', 'created_at']
    search_fields = ['name', 'slug', 'description']
    ordering = ['sort_order', 'id']
    readonly_fields = ['id', 'created_at', 'updated_at', 'posts_count']
    
    fieldsets = (
        ('基本信息', {
            'fields': ('id', 'slug', 'name', 'description')
        }),
        ('显示配置', {
            'fields': ('color', 'icon_file_id', 'sort_order')
        }),
        ('状态', {
            'fields': ('is_enabled', 'posts_count')
        }),
        ('时间信息', {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )
    
    def has_delete_permission(self, request, obj=None):
        """禁止直接删除"""
        return False

