"""
Section serializers
"""
from rest_framework import serializers
from .models import Section
from apps.core.file_service_client import file_service_client


class SectionListSerializer(serializers.ModelSerializer):
    """
    板块列表序列化器（用于列表展示）
    """
    
    icon_url = serializers.SerializerMethodField()
    
    class Meta:
        model = Section
        fields = [
            'id',
            'slug',
            'name',
            'description',
            'icon_url',
            'color',
            'sort_order',
            'posts_count',
            'created_at',
            'updated_at'
        ]
        read_only_fields = ['id', 'created_at', 'updated_at', 'posts_count']
    
    def get_icon_url(self, obj):
        """
        获取图标 URL（通过 M7 文件服务）
        """
        if not obj.icon_file_id:
            return None
        
        # 调用文件服务获取 URL
        return file_service_client.get_file_url(str(obj.icon_file_id))


class SectionDetailSerializer(serializers.ModelSerializer):
    """
    板块详情序列化器（包含更多信息）
    """
    
    icon_url = serializers.SerializerMethodField()
    
    class Meta:
        model = Section
        fields = [
            'id',
            'slug',
            'name',
            'description',
            'icon_file_id',
            'icon_url',
            'color',
            'sort_order',
            'is_enabled',
            'posts_count',
            'created_at',
            'updated_at'
        ]
        read_only_fields = ['id', 'created_at', 'updated_at', 'posts_count']
    
    def get_icon_url(self, obj):
        """获取图标 URL（通过 M7 文件服务）"""
        if not obj.icon_file_id:
            return None
        return file_service_client.get_file_url(str(obj.icon_file_id))


class SectionCreateSerializer(serializers.ModelSerializer):
    """
    创建板块序列化器
    """
    
    class Meta:
        model = Section
        fields = [
            'slug',
            'name',
            'description',
            'icon_file_id',
            'color',
            'sort_order'
        ]
    
    def validate_slug(self, value):
        """验证 slug 格式和唯一性"""
        if len(value) < 3 or len(value) > 20:
            raise serializers.ValidationError('板块标识长度必须在3-20个字符之间')
        
        if Section.objects.filter(slug=value).exists():
            raise serializers.ValidationError('该板块标识已存在')
        
        return value
    
    def validate_name(self, value):
        """验证 name 唯一性"""
        if len(value) < 2 or len(value) > 50:
            raise serializers.ValidationError('板块名称长度必须在2-50个字符之间')
        
        if Section.objects.filter(name=value).exists():
            raise serializers.ValidationError('该板块名称已存在')
        
        return value


class SectionUpdateSerializer(serializers.ModelSerializer):
    """
    更新板块序列化器（允许部分更新）
    """
    
    class Meta:
        model = Section
        fields = [
            'name',
            'description',
            'icon_file_id',
            'color',
            'sort_order'
        ]
    
    def validate_name(self, value):
        """验证 name 唯一性（排除自己）"""
        if len(value) < 2 or len(value) > 50:
            raise serializers.ValidationError('板块名称长度必须在2-50个字符之间')
        
        # 排除当前对象
        if Section.objects.filter(name=value).exclude(id=self.instance.id).exists():
            raise serializers.ValidationError('该板块名称已存在')
        
        return value


class SectionStatusSerializer(serializers.Serializer):
    """
    更新板块状态序列化器
    """
    is_enabled = serializers.BooleanField(required=True)


class SectionReorderSerializer(serializers.Serializer):
    """
    批量调整排序序列化器
    """
    sections = serializers.ListField(
        child=serializers.DictField(),
        min_length=1,
        max_length=100
    )
    
    def validate_sections(self, value):
        """
        验证排序数据
        
        预期格式: [{"id": 1, "sort_order": 10}, ...]
        """
        for item in value:
            if 'id' not in item or 'sort_order' not in item:
                raise serializers.ValidationError('每项必须包含 id 和 sort_order')
            
            if not isinstance(item['id'], int) or item['id'] <= 0:
                raise serializers.ValidationError('id 必须是正整数')
            
            if not isinstance(item['sort_order'], int):
                raise serializers.ValidationError('sort_order 必须是整数')
            
            if item['sort_order'] < 1 or item['sort_order'] > 999:
                raise serializers.ValidationError('sort_order 必须在 1-999 之间')
        
        return value


class SectionStatisticsSerializer(serializers.Serializer):
    """
    板块统计序列化器（用于响应）
    """
    total_sections = serializers.IntegerField()
    enabled_sections = serializers.IntegerField()
    disabled_sections = serializers.IntegerField()
    total_posts = serializers.IntegerField()
    sections = serializers.ListField(
        child=serializers.DictField()
    )

