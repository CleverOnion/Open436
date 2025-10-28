-- ========================================
-- Sa-Token 认证插件 Schema
-- ========================================

return {
  name = "sa-token-auth",
  fields = {
    {
      config = {
        type = "record",
        fields = {
          {
            auth_service_url = {
              type = "string",
              default = "http://auth-service:8001",
              required = false,
              description = "M1 认证服务地址"
            }
          },
          {
            cache_ttl = {
              type = "number",
              default = 300,
              required = false,
              description = "Token 验证结果缓存时间（秒）"
            }
          }
        }
      }
    }
  }
}

