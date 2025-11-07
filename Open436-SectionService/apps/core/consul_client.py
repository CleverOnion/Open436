"""
Consul service discovery client
"""
import consul
import logging
import socket
from typing import Optional, Dict, Any
from django.conf import settings

logger = logging.getLogger(__name__)


class ConsulClient:
    """
    Consul 客户端
    
    提供服务注册、注销和服务发现功能
    """
    
    def __init__(self):
        """初始化 Consul 客户端"""
        self.consul_url = settings.CONSUL_URL
        host, port = self._parse_consul_url(self.consul_url)
        
        try:
            self.client = consul.Consul(host=host, port=port)
            logger.info(f"Consul client initialized: {host}:{port}")
        except Exception as e:
            logger.error(f"Failed to initialize Consul client: {e}")
            self.client = None
    
    def _parse_consul_url(self, url: str) -> tuple:
        """解析 Consul URL"""
        # http://localhost:8500 -> (localhost, 8500)
        url = url.replace('http://', '').replace('https://', '')
        if ':' in url:
            host, port = url.split(':')
            return host, int(port)
        return url, 8500
    
    def register_service(self) -> bool:
        """
        注册服务到 Consul
        
        Returns:
            bool: 注册是否成功
        """
        if not self.client:
            logger.warning("Consul client not available, skipping registration")
            return False
        
        service_id = settings.CONSUL_SERVICE_ID
        service_name = settings.CONSUL_SERVICE_NAME
        service_port = settings.SERVICE_PORT
        service_host = self._get_service_host()
        
        # 健康检查配置
        check = consul.Check.http(
            url=f'http://{service_host}:{service_port}/health/',
            interval='10s',
            timeout='5s',
            deregister='30s'
        )
        
        try:
            self.client.agent.service.register(
                name=service_name,
                service_id=service_id,
                address=service_host,
                port=service_port,
                check=check,
                tags=['django', 'section-service', 'v1.0']
            )
            logger.info(f"Service registered to Consul: {service_id} at {service_host}:{service_port}")
            return True
        except Exception as e:
            logger.error(f"Failed to register service to Consul: {e}")
            return False
    
    def deregister_service(self) -> bool:
        """
        从 Consul 注销服务
        
        Returns:
            bool: 注销是否成功
        """
        if not self.client:
            return False
        
        service_id = settings.CONSUL_SERVICE_ID
        
        try:
            self.client.agent.service.deregister(service_id)
            logger.info(f"Service deregistered from Consul: {service_id}")
            return True
        except Exception as e:
            logger.error(f"Failed to deregister service from Consul: {e}")
            return False
    
    def discover_service(self, service_name: str) -> Optional[Dict[str, Any]]:
        """
        发现服务实例
        
        Args:
            service_name: 服务名称（如 'file-service'）
        
        Returns:
            服务实例信息，包含 host 和 port
        """
        if not self.client:
            logger.warning(f"Consul client not available, cannot discover {service_name}")
            return None
        
        try:
            # 获取健康的服务实例
            index, services = self.client.health.service(service_name, passing=True)
            
            if not services:
                logger.warning(f"No healthy instances found for service: {service_name}")
                return None
            
            # 返回第一个实例（简单负载均衡可以随机选择）
            service = services[0]
            return {
                'host': service['Service']['Address'],
                'port': service['Service']['Port'],
                'service_id': service['Service']['ID']
            }
        except Exception as e:
            logger.error(f"Failed to discover service {service_name}: {e}")
            return None
    
    def _get_service_host(self) -> str:
        """
        获取服务主机地址
        
        Returns:
            服务主机 IP 地址
        """
        # 在容器环境中，使用容器的 IP
        # 在开发环境中，使用 localhost
        if settings.DEBUG:
            # 开发环境：尝试获取本机IP
            try:
                s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
                s.connect(('8.8.8.8', 80))
                ip = s.getsockname()[0]
                s.close()
                return ip
            except Exception:
                return 'localhost'
        else:
            # 生产环境：从环境变量或容器信息获取
            return socket.gethostbyname(socket.gethostname())


# 全局 Consul 客户端实例
consul_client = ConsulClient()

