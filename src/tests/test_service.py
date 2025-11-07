"""
板块服务层测试
"""
import pytest
from src.app.services.section_service import SectionService
from src.app.models.schemas import SectionCreate, SectionUpdate


class TestSectionService:
    """板块服务测试类"""
    
    def test_create_section(self, db_session):
        """测试创建板块服务"""
        service = SectionService(db_session)
        
        section_data = SectionCreate(
            slug="test",
            name="测试板块",
            description="测试描述",
            color="#1976D2",
            sort_order=10
        )
        
        section = service.create_section(section_data)
        
        assert section.id is not None
        assert section.slug == "test"
        assert section.name == "测试板块"
        assert section.is_enabled is True
        assert section.posts_count == 0
    
    def test_get_section_by_slug(self, db_session):
        """测试通过slug获取板块"""
        service = SectionService(db_session)
        
        # 创建板块
        section_data = SectionCreate(
            slug="test",
            name="测试板块",
            color="#1976D2",
            sort_order=10
        )
        created = service.create_section(section_data)
        
        # 获取板块
        section = service.get_section_by_slug("test")
        assert section is not None
        assert section.id == created.id
    
    def test_update_section(self, db_session):
        """测试更新板块"""
        service = SectionService(db_session)
        
        # 创建板块
        section_data = SectionCreate(
            slug="test",
            name="测试板块",
            color="#1976D2",
            sort_order=10
        )
        created = service.create_section(section_data)
        
        # 更新板块
        update_data = SectionUpdate(
            name="更新后的名称",
            color="#FF0000"
        )
        updated = service.update_section(created.id, update_data)
        
        assert updated.name == "更新后的名称"
        assert updated.color == "#FF0000"
        assert updated.slug == "test"  # slug不应被修改
    
    def test_toggle_section(self, db_session):
        """测试切换板块状态"""
        service = SectionService(db_session)
        
        # 创建板块
        section_data = SectionCreate(
            slug="test",
            name="测试板块",
            color="#1976D2",
            sort_order=10
        )
        created = service.create_section(section_data)
        assert created.is_enabled is True
        
        # 第一次切换
        toggled = service.toggle_section(created.id)
        assert toggled.is_enabled is False
        
        # 第二次切换
        toggled = service.toggle_section(created.id)
        assert toggled.is_enabled is True
    
    def test_increment_posts_count(self, db_session):
        """测试增加帖子数量"""
        service = SectionService(db_session)
        
        # 创建板块
        section_data = SectionCreate(
            slug="test",
            name="测试板块",
            color="#1976D2",
            sort_order=10
        )
        created = service.create_section(section_data)
        assert created.posts_count == 0
        
        # 增加帖子数
        service.increment_posts_count(created.id)
        section = service.get_section_by_id(created.id)
        assert section.posts_count == 1
        
        # 再次增加
        service.increment_posts_count(created.id)
        section = service.get_section_by_id(created.id)
        assert section.posts_count == 2
    
    def test_decrement_posts_count(self, db_session):
        """测试减少帖子数量"""
        service = SectionService(db_session)
        
        # 创建板块并增加帖子数
        section_data = SectionCreate(
            slug="test",
            name="测试板块",
            color="#1976D2",
            sort_order=10
        )
        created = service.create_section(section_data)
        service.increment_posts_count(created.id)
        service.increment_posts_count(created.id)
        
        # 减少帖子数
        service.decrement_posts_count(created.id)
        section = service.get_section_by_id(created.id)
        assert section.posts_count == 1
    
    def test_count_enabled_sections(self, db_session):
        """测试统计启用的板块数量"""
        service = SectionService(db_session)
        
        # 创建多个板块
        for i in range(3):
            section_data = SectionCreate(
                slug=f"test{i}",
                name=f"测试板块{i}",
                color="#1976D2",
                sort_order=i
            )
            service.create_section(section_data)
        
        # 统计启用的板块
        count = service.count_enabled_sections()
        assert count == 3
        
        # 禁用一个板块
        sections, _ = service.get_sections(page=1, page_size=10, enabled_only=False)
        service.toggle_section(sections[0].id)
        
        # 再次统计
        count = service.count_enabled_sections()
        assert count == 2

