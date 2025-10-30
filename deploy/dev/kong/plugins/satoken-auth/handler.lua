local http = require "resty.http"
local cjson = require "cjson"

local SaTokenAuthHandler = {
  VERSION = "1.0.0",
  PRIORITY = 1000,
}

function SaTokenAuthHandler:access(conf)
  -- 1. 获取 Token
  local token = kong.request.get_header(conf.token_header_name)
  
  if not token then
    return kong.response.exit(401, { message = "No token provided" })
  end
  
  -- 移除 "Bearer " 前缀
  token = token:gsub("Bearer ", "")
  
  -- 2. 调用认证服务验证 Token
  local httpc = http.new()
  httpc:set_timeout(5000)
  
  local verify_url = conf.auth_service_url .. "/api/auth/verify"
  
  local res, err = httpc:request_uri(verify_url, {
    method = "POST",
    headers = {
      ["Content-Type"] = "application/json",
    },
    body = cjson.encode({ token = token })
  })
  
  if not res then
    kong.log.err("Failed to call auth service: ", err)
    return kong.response.exit(503, { message = "Auth service unavailable" })
  end
  
  if res.status ~= 200 then
    return kong.response.exit(401, { message = "Invalid token" })
  end
  
  -- 3. 解析响应
  local body = cjson.decode(res.body)
  
  if not body.data or not body.data.valid then
    return kong.response.exit(401, { message = "Token verification failed" })
  end
  
  -- 4. 注入用户信息到 Header (注意数据结构是嵌套的 body.data.data)
  local userData = body.data.data or body.data
  kong.service.request.set_header("X-User-Id", tostring(userData.userId))
  
  -- 只有当字段存在时才设置 header
  if userData.username then
    kong.service.request.set_header("X-Username", userData.username)
  end
  if userData.role then
    kong.service.request.set_header("X-Role", userData.role)
    kong.service.request.set_header("X-User-Role", userData.role)  -- M7 expects X-User-Role
  end
  
  kong.log.info("User authenticated: ", userData.username or "unknown", " role: ", userData.role or "none")
end

return SaTokenAuthHandler


