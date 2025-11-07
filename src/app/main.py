"""
FastAPI 应用主入口
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from src.config import settings
from src.app.routes import sections
from src.app.models.database import engine, Base


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理"""
    # 启动时：创建数据库表
    print("🚀 启动板块管理服务...")
    Base.metadata.create_all(bind=engine)
    print("✅ 数据库表初始化完成")
    
    yield
    
    # 关闭时：清理资源
    print("👋 关闭板块管理服务...")


# 创建FastAPI应用实例
app = FastAPI(
    title=settings.API_TITLE,
    description=settings.API_DESCRIPTION,
    version=settings.API_VERSION,
    lifespan=lifespan
)

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(
    sections.router,
    prefix=f"{settings.API_PREFIX}/sections",
    tags=["板块管理"]
)


@app.get("/")
async def root():
    """根路径"""
    return {
        "service": settings.API_TITLE,
        "version": settings.API_VERSION,
        "status": "running"
    }


@app.get("/health")
async def health_check():
    """健康检查"""
    return {"status": "healthy"}

