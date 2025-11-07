"""
File Service (M7) client
"""
import requests
import logging
from typing import Optional
from django.conf import settings
from django.core.cache import cache
from .consul_client import consul_client

logger = logging.getLogger(__name__)


class FileServiceClient:
    """
    文件服务客户端
    
    通过 Consul 发现 M7 文件服务并调用其 API
    """
    
    def __init__(self):
        """初始化文件服务客户端"""
        self.service_name = settings.FILE_SERVICE_NAME
        self.timeout = 5  # 请求超时（秒）
    
    def _get_service_url(self) -> Optional[str]:
        """
        获取文件服务 URL（通过 Consul 服务发现）
        
        Returns:
            文件服务的基础 URL
        """
        # 尝试从缓存获取
        cache_key = f'service_url:{self.service_name}'
        cached_url = cache.get(cache_key)
        if cached_url:
            return cached_url
        
        # 通过 Consul 发现服务
        service_info = consul_client.discover_service(self.service_name)
        if not service_info:
            logger.warning(f"Cannot discover {self.service_name}")
            return None
        
        # 构建 URL
        base_url = f"http://{service_info['host']}:{service_info['port']}"
        
        # 缓存 URL（5分钟）
        cache.set(cache_key, base_url, 300)
        
        return base_url
    
    def get_file_url(self, file_id: str) -> Optional[str]:
        """
        获取文件访问 URL
        
        Args:
            file_id: 文件 UUID
        
        Returns:
            文件的访问 URL（如果成功）
        """
        if not file_id:
            return None
        
        # 尝试从缓存获取
        cache_key = f'file_url:{file_id}'
        cached_url = cache.get(cache_key)
        if cached_url:
            return cached_url
        
        # 获取服务 URL
        base_url = self._get_service_url()
        if not base_url:
            logger.error("File service not available")
            return None
        
        # 调用文件服务 API
        try:
            response = requests.get(
                f"{base_url}/files/{file_id}",
                timeout=self.timeout
            )
            
            if response.status_code == 200:
                data = response.json()
                # 文件服务应该返回 {"data": {"url": "..."}}
                if 'data' in data and 'url' in data['data']:
                    url = data['data']['url']
                    # 缓存 URL（10分钟）
                    cache.set(cache_key, url, 600)
                    return url
            else:
                logger.warning(f"Failed to get file URL for {file_id}: HTTP {response.status_code}")
                return None
        except requests.exceptions.Timeout:
            logger.error(f"Timeout when requesting file {file_id}")
            return None
        except requests.exceptions.RequestException as e:
            logger.error(f"Error requesting file {file_id}: {e}")
            return None
        except Exception as e:
            logger.error(f"Unexpected error getting file URL: {e}")
            return None


# 全局文件服务客户端实例
file_service_client = FileServiceClient()

