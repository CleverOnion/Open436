/**
 * 全局常量定义
 */

// 存储键名
export const STORAGE_KEYS = {
  TOKEN: 'token',
  USER_INFO: 'user_info',
  THEME: 'theme'
}

// API 路由前缀
export const API_PREFIX = {
  AUTH: '/api/auth',      // M1 认证服务
  USER: '/api/users',     // M2 用户服务
  POST: '/api/posts',     // M3 内容服务
  COMMENT: '/api/comments', // M4 评论服务
  SECTION: '/api/sections', // M5 板块服务
  SEARCH: '/api/search',  // M6 搜索服务
  FILE: '/api/files'      // M7 文件服务
}

// HTTP 状态码
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  INTERNAL_SERVER_ERROR: 500
}

// 业务状态码（根据后端约定调整）
export const BUSINESS_CODE = {
  SUCCESS: 200,
  FAILED: 500,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403
}

export default {
  STORAGE_KEYS,
  API_PREFIX,
  HTTP_STATUS,
  BUSINESS_CODE
}

