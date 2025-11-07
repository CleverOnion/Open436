"""
应用配置模块
从环境变量加载配置
"""
from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    """应用配置类"""
    
    # 数据库配置
    DATABASE_URL: str = "postgresql://open436:open436_password@localhost:55432/open436"
    
    # API配置
    API_PREFIX: str = "/api/v1"
    API_TITLE: str = "板块管理服务"
    API_VERSION: str = "1.0.0"
    API_DESCRIPTION: str = "论坛板块管理模块 API"
    
    # CORS配置
    CORS_ORIGINS: List[str] = ["http://localhost:3000", "http://localhost:8080"]
    
    # 日志配置
    LOG_LEVEL: str = "INFO"
    
    # 认证配置（与M1用户服务对接）
    AUTH_SERVICE_URL: str = "http://localhost:8001"
    JWT_SECRET_KEY: str = "your-secret-key-here"
    JWT_ALGORITHM: str = "HS256"
    
    # 文件服务配置（与M7文件服务对接）
    FILE_SERVICE_URL: str = "http://localhost:8007"
    
    # 分页配置
    DEFAULT_PAGE_SIZE: int = 20
    MAX_PAGE_SIZE: int = 100
    
    class Config:
        env_file = ".env"
        case_sensitive = True


# 创建全局配置实例
settings = Settings()

