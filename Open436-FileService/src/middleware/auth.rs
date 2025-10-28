use actix_web::HttpRequest;
use crate::utils::error::FileError;

/// 从 Kong 网关传递的请求头中提取用户 ID
pub fn get_current_user_id(req: &HttpRequest) -> Result<i32, FileError> {
    req.headers()
        .get("X-User-Id")
        .and_then(|v| v.to_str().ok())
        .and_then(|s| s.parse::<i32>().ok())
        .ok_or(FileError::Unauthorized)
}

/// 检查是否为管理员
pub fn is_admin(req: &HttpRequest) -> Result<bool, FileError> {
    let role = req
        .headers()
        .get("X-User-Role")
        .and_then(|v| v.to_str().ok())
        .unwrap_or("");

    Ok(role == "admin")
}

/// 验证管理员权限
pub fn require_admin(req: &HttpRequest) -> Result<(), FileError> {
    if is_admin(req)? {
        Ok(())
    } else {
        Err(FileError::Forbidden)
    }
}

