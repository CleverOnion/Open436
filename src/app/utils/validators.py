"""
数据验证工具
"""
import re
from typing import Optional


def validate_hex_color(color: str) -> bool:
    """
    验证HEX颜色格式
    
    Args:
        color: 颜色字符串
        
    Returns:
        是否有效
    """
    pattern = r'^#[0-9A-Fa-f]{6}$'
    return bool(re.match(pattern, color))


def validate_slug(slug: str) -> bool:
    """
    验证slug格式
    
    Args:
        slug: 板块标识
        
    Returns:
        是否有效
    """
    pattern = r'^[a-z0-9_]{3,20}$'
    return bool(re.match(pattern, slug))


def sanitize_string(text: Optional[str], max_length: int = 1000) -> Optional[str]:
    """
    清理字符串（防止XSS等）
    
    Args:
        text: 原始文本
        max_length: 最大长度
        
    Returns:
        清理后的文本
    """
    if not text:
        return text
    
    # 移除两端空白
    text = text.strip()
    
    # 限制长度
    if len(text) > max_length:
        text = text[:max_length]
    
    return text

