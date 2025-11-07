"""
Tests for Section API
"""
from django.test import TestCase
from django.urls import reverse
from rest_framework.test import APIClient
from rest_framework import status
from apps.sections.models import Section


class SectionAPITest(TestCase):
    """测试 Section API"""
    
    def setUp(self):
        """设置测试数据"""
        self.client = APIClient()
        
        # 创建测试板块
        self.section1 = Section.objects.create(
            slug='tech',
            name='技术交流',
            description='技术讨论',
            color='#1976D2',
            sort_order=1
        )
        
        self.section2 = Section.objects.create(
            slug='design',
            name='设计分享',
            description='设计作品',
            color='#9C27B0',
            sort_order=2
        )
    
    def test_list_sections(self):
        """测试获取板块列表"""
        url = reverse('section-list')
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('data', response.json())
    
    def test_retrieve_section_by_id(self):
        """测试通过ID获取板块详情"""
        url = reverse('section-detail', args=[self.section1.id])
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        data = response.json()['data']
        self.assertEqual(data['slug'], 'tech')
    
    def test_retrieve_section_by_slug(self):
        """测试通过slug获取板块详情"""
        url = reverse('section-detail', args=['tech'])
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        data = response.json()['data']
        self.assertEqual(data['slug'], 'tech')
    
    def test_create_section_without_auth(self):
        """测试未认证创建板块（应该失败）"""
        url = reverse('section-list')
        data = {
            'slug': 'new',
            'name': '新板块',
            'color': '#FF0000',
            'sort_order': 10
        }
        response = self.client.post(url, data)
        
        # 因为没有管理员权限，应该返回 403
        self.assertIn(response.status_code, [status.HTTP_401_UNAUTHORIZED, status.HTTP_403_FORBIDDEN])

