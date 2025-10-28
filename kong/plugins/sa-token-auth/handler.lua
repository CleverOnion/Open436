-- ========================================
-- Sa-Token 认证插件
-- ========================================
-- 用途: 验证 M1 认证服务生成的 Sa-Token
-- 流程: 调用 M1 /api/auth/verify 接口验证 Token
-- ========================================

local http = require "resty.http"
local cjson = require "cjson"

local SaTokenAuthHandler = {
  VERSION = "1.0.0",
  PRIORITY = 1000,
}

--- 从请求头中提取 Token
local function extract_token()
  local authorization = kong.request.get_header("Authorization")
  
  if not authorization then
    return nil, "No Authorization header"
  end
  
  -- 移除 "Bearer " 前缀
  local token = string.match(authorization, "Bearer%s+(.+)")
  
  if not token then
    return nil, "Invalid Authorization header format"
  end
  
  return token, nil
end

--- 调用 M1 验证 Token
local function verify_token(token, auth_service_url)
  local httpc = http.new()
  httpc:set_timeout(5000)  -- 5 秒超时
  
  local verify_url = auth_service_url .. "/api/auth/verify"
  
  local res, err = httpc:request_uri(verify_url, {
    method = "GET",
    headers = {
      ["Authorization"] = "Bearer " .. token,
      ["Content-Type"] = "application/json"
    }
  })
  
  if not res then
    kong.log.err("Failed to connect to auth service: ", err)
    return nil, "Failed to verify token"
  end
  
  if res.status ~= 200 then
    kong.log.warn("Token verification failed with status: ", res.status)
    return nil, "Invalid or expired token"
  end
  
  local ok, response_data = pcall(cjson.decode, res.body)
  if not ok then
    kong.log.err("Failed to parse auth service response: ", response_data)
    return nil, "Invalid auth service response"
  end
  
  return response_data, nil
end

--- access 阶段：验证 Token 并注入用户信息
function SaTokenAuthHandler:access(conf)
  -- 1. 提取 Token
  local token, err = extract_token()
  if err then
    kong.log.warn("Token extraction failed: ", err)
    return kong.response.exit(401, {
      code = 70001006,
      message = "Authentication required",
      timestamp = os.date("!%Y-%m-%dT%H:%M:%SZ")
    })
  end
  
  -- 2. 调用 M1 验证 Token
  local user_data, err = verify_token(token, conf.auth_service_url)
  if err then
    kong.log.warn("Token verification failed: ", err)
    return kong.response.exit(401, {
      code = 70001006,
      message = err,
      timestamp = os.date("!%Y-%m-%dT%H:%M:%SZ")
    })
  end
  
  -- 3. 提取用户信息
  local user_id = user_data.data and user_data.data.userId
  local role = user_data.data and user_data.data.role or "user"
  local username = user_data.data and user_data.data.username
  
  if not user_id then
    kong.log.err("User ID not found in auth response")
    return kong.response.exit(500, {
      code = 70009999,
      message = "Internal server error",
      timestamp = os.date("!%Y-%m-%dT%H:%M:%SZ")
    })
  end
  
  -- 4. 注入用户信息到请求头（传递给后端服务）
  kong.service.request.set_header("X-User-Id", tostring(user_id))
  kong.service.request.set_header("X-User-Role", role)
  kong.service.request.set_header("X-Username", username or "")
  kong.service.request.set_header("X-Kong-Gateway", "true")
  
  kong.log.info("User authenticated: ", user_id, " (", role, ")")
end

return SaTokenAuthHandler

