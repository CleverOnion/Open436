use actix_web::{dev::ServiceRequest, Error, HttpMessage, HttpRequest};

/// 用户信息结构（从 Kong 注入的 Header 中提取）
#[derive(Clone, Debug)]
pub struct UserInfo {
    pub user_id: i64,
    pub username: String,
    pub role: String,
}

/// 从请求 Header 中提取用户信息
pub fn extract_user_info(req: &ServiceRequest) -> Option<UserInfo> {
    let headers = req.headers();
    
    // 从 Kong 注入的 Header 获取用户信息
    let user_id = headers.get("X-User-Id")?.to_str().ok()?.parse::<i64>().ok()?;
    let username = headers.get("X-Username")?.to_str().ok()?.to_string();
    let role = headers.get("X-Role")?.to_str().ok()?.to_string();
    
    Some(UserInfo {
        user_id,
        username,
        role,
    })
}

/// 从请求中获取用户信息
pub fn get_user_info(req: &HttpRequest) -> Option<UserInfo> {
    req.extensions().get::<UserInfo>().cloned()
}

