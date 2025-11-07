"""
Tests for Section model
"""
from django.test import TestCase
from apps.sections.models import Section


class SectionModelTest(TestCase):
    """测试 Section 模型"""
    
    def setUp(self):
        """设置测试数据"""
        self.section = Section.objects.create(
            slug='test',
            name='测试板块',
            description='测试描述',
            color='#1976D2',
            sort_order=100
        )
    
    def test_section_creation(self):
        """测试板块创建"""
        self.assertEqual(self.section.slug, 'test')
        self.assertEqual(self.section.name, '测试板块')
        self.assertTrue(self.section.is_enabled)
        self.assertEqual(self.section.posts_count, 0)
    
    def test_section_str(self):
        """测试 __str__ 方法"""
        self.assertEqual(str(self.section), '测试板块 (test)')
    
    def test_get_enabled_sections(self):
        """测试获取启用的板块"""
        Section.objects.create(
            slug='disabled',
            name='禁用板块',
            color='#FF0000',
            sort_order=200,
            is_enabled=False
        )
        
        enabled = Section.get_enabled_sections()
        self.assertEqual(enabled.count(), 1)
        self.assertEqual(enabled.first().slug, 'test')
    
    def test_can_be_deleted(self):
        """测试删除检查"""
        can_delete, reason = self.section.can_be_deleted()
        self.assertTrue(can_delete)
        
        # 有帖子时不能删除
        self.section.posts_count = 10
        self.section.save()
        can_delete, reason = self.section.can_be_deleted()
        self.assertFalse(can_delete)

