"""
板块功能测试
"""
import pytest
from fastapi import status


class TestSectionAPI:
    """板块API测试类"""
    
    def test_create_section(self, client, sample_section_data):
        """测试创建板块"""
        response = client.post("/api/v1/sections/", json=sample_section_data)
        assert response.status_code == status.HTTP_201_CREATED
        
        data = response.json()
        assert data["slug"] == sample_section_data["slug"]
        assert data["name"] == sample_section_data["name"]
        assert data["is_enabled"] is True
        assert data["posts_count"] == 0
    
    def test_create_section_duplicate_slug(self, client, sample_section_data):
        """测试创建重复slug的板块"""
        # 创建第一个板块
        client.post("/api/v1/sections/", json=sample_section_data)
        
        # 尝试创建相同slug的板块
        response = client.post("/api/v1/sections/", json=sample_section_data)
        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert "已存在" in response.json()["detail"]
    
    def test_create_section_invalid_slug(self, client, sample_section_data):
        """测试创建无效slug的板块"""
        invalid_data = sample_section_data.copy()
        invalid_data["slug"] = "Test_Invalid"  # 包含大写字母
        
        response = client.post("/api/v1/sections/", json=invalid_data)
        assert response.status_code == status.HTTP_422_UNPROCESSABLE_ENTITY
    
    def test_get_sections_list(self, client, sample_section_data):
        """测试获取板块列表"""
        # 创建一个板块
        client.post("/api/v1/sections/", json=sample_section_data)
        
        # 获取列表
        response = client.get("/api/v1/sections/")
        assert response.status_code == status.HTTP_200_OK
        
        data = response.json()
        assert "total" in data
        assert "items" in data
        assert data["total"] >= 1
    
    def test_get_section_by_id(self, client, sample_section_data):
        """测试通过ID获取板块"""
        # 创建板块
        create_response = client.post("/api/v1/sections/", json=sample_section_data)
        section_id = create_response.json()["id"]
        
        # 获取板块
        response = client.get(f"/api/v1/sections/{section_id}")
        assert response.status_code == status.HTTP_200_OK
        
        data = response.json()
        assert data["id"] == section_id
        assert data["slug"] == sample_section_data["slug"]
    
    def test_get_section_by_slug(self, client, sample_section_data):
        """测试通过slug获取板块"""
        # 创建板块
        client.post("/api/v1/sections/", json=sample_section_data)
        
        # 通过slug获取
        response = client.get(f"/api/v1/sections/slug/{sample_section_data['slug']}")
        assert response.status_code == status.HTTP_200_OK
        
        data = response.json()
        assert data["slug"] == sample_section_data["slug"]
    
    def test_update_section(self, client, sample_section_data):
        """测试更新板块"""
        # 创建板块
        create_response = client.post("/api/v1/sections/", json=sample_section_data)
        section_id = create_response.json()["id"]
        
        # 更新板块
        update_data = {
            "name": "更新后的板块",
            "color": "#FF0000",
            "sort_order": 20
        }
        response = client.put(f"/api/v1/sections/{section_id}", json=update_data)
        assert response.status_code == status.HTTP_200_OK
        
        data = response.json()
        assert data["name"] == update_data["name"]
        assert data["color"] == update_data["color"]
        assert data["sort_order"] == update_data["sort_order"]
    
    def test_delete_section_soft(self, client, sample_section_data):
        """测试软删除板块"""
        # 创建板块
        create_response = client.post("/api/v1/sections/", json=sample_section_data)
        section_id = create_response.json()["id"]
        
        # 软删除
        response = client.delete(f"/api/v1/sections/{section_id}")
        assert response.status_code == status.HTTP_200_OK
        
        # 验证板块被禁用
        get_response = client.get(f"/api/v1/sections/{section_id}")
        assert get_response.json()["is_enabled"] is False
    
    def test_toggle_section(self, client, sample_section_data):
        """测试切换板块状态"""
        # 创建板块
        create_response = client.post("/api/v1/sections/", json=sample_section_data)
        section_id = create_response.json()["id"]
        
        # 第一次切换（禁用）
        response = client.patch(f"/api/v1/sections/{section_id}/toggle")
        assert response.status_code == status.HTTP_200_OK
        assert response.json()["is_enabled"] is False
        
        # 第二次切换（启用）
        response = client.patch(f"/api/v1/sections/{section_id}/toggle")
        assert response.status_code == status.HTTP_200_OK
        assert response.json()["is_enabled"] is True
    
    def test_pagination(self, client):
        """测试分页功能"""
        # 创建多个板块
        for i in range(5):
            data = {
                "slug": f"test{i}",
                "name": f"测试板块{i}",
                "color": "#1976D2",
                "sort_order": i
            }
            client.post("/api/v1/sections/", json=data)
        
        # 测试分页
        response = client.get("/api/v1/sections/?page=1&page_size=3")
        assert response.status_code == status.HTTP_200_OK
        
        data = response.json()
        assert len(data["items"]) <= 3
        assert data["page"] == 1
        assert data["page_size"] == 3


class TestSectionValidation:
    """板块验证测试类"""
    
    def test_invalid_color_format(self, client, sample_section_data):
        """测试无效的颜色格式"""
        invalid_data = sample_section_data.copy()
        invalid_data["color"] = "blue"  # 不是HEX格式
        
        response = client.post("/api/v1/sections/", json=invalid_data)
        assert response.status_code == status.HTTP_422_UNPROCESSABLE_ENTITY
    
    def test_sort_order_range(self, client, sample_section_data):
        """测试排序号范围"""
        # 测试超出上限
        invalid_data = sample_section_data.copy()
        invalid_data["sort_order"] = 1000
        
        response = client.post("/api/v1/sections/", json=invalid_data)
        assert response.status_code == status.HTTP_422_UNPROCESSABLE_ENTITY
        
        # 测试低于下限
        invalid_data["sort_order"] = 0
        response = client.post("/api/v1/sections/", json=invalid_data)
        assert response.status_code == status.HTTP_422_UNPROCESSABLE_ENTITY
    
    def test_name_length_validation(self, client, sample_section_data):
        """测试名称长度验证"""
        # 名称过短
        invalid_data = sample_section_data.copy()
        invalid_data["name"] = "A"
        
        response = client.post("/api/v1/sections/", json=invalid_data)
        assert response.status_code == status.HTTP_422_UNPROCESSABLE_ENTITY
        
        # 名称过长
        invalid_data["name"] = "A" * 51
        response = client.post("/api/v1/sections/", json=invalid_data)
        assert response.status_code == status.HTTP_422_UNPROCESSABLE_ENTITY

