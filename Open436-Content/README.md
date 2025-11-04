# Open436-Content 内容管理模块

## 项目简介

Open436-Content 是 Open436 论坛系统的内容管理模块，负责帖子的完整生命周期管理。

**当前状态**：框架搭建阶段（v1.0.0）

本项目已完成基础框架搭建，包括完整的分层架构、数据库设计、API接口定义等。具体业务逻辑待后续实现。

## 功能特性

### 核心功能

- ✅ 发布新帖
- ✅ 浏览帖子列表（支持分页、筛选、排序）
- ✅ 查看帖子详情
- ✅ 编辑帖子
- ✅ 删除帖子
- ✅ 查看用户的帖子列表

### 管理员功能

- ✅ 置顶/取消置顶帖子
- ✅ 恢复已删除的帖子
- ✅ 永久删除帖子
- ✅ 查看帖子编辑历史

## 技术栈

- **后端框架**：Spring Boot 3.5.7
- **数据库**：PostgreSQL
- **ORM框架**：Spring Data JPA / Hibernate
- **API文档**：Springdoc OpenAPI 3.0 (Swagger)
- **构建工具**：Maven
- **JDK版本**：Java 17

## 项目结构

```
Open436-Content/
├── src/
│   ├── main/
│   │   ├── java/com/open436/content/
│   │   │   ├── common/              # 通用类
│   │   │   │   ├── Result.java      # 统一响应结果
│   │   │   │   ├── PageResult.java  # 分页结果
│   │   │   │   └── exception/       # 异常类
│   │   │   ├── config/              # 配置类
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   └── JpaConfig.java
│   │   │   ├── controller/          # 控制器层
│   │   │   │   ├── PostController.java
│   │   │   │   └── PostManageController.java
│   │   │   ├── dto/                 # 数据传输对象
│   │   │   ├── entity/              # 实体类
│   │   │   │   ├── Post.java
│   │   │   │   ├── PostEditHistory.java
│   │   │   │   └── PostViewRecord.java
│   │   │   ├── repository/          # 数据访问层
│   │   │   ├── service/             # 业务逻辑层
│   │   │   │   ├── PostService.java
│   │   │   │   ├── PostManageService.java
│   │   │   │   └── impl/
│   │   │   └── vo/                  # 视图对象
│   │   └── resources/
│   │       ├── application.yml      # 应用配置
│   │       └── db/
│   │           └── schema.sql       # 数据库表结构
│   └── test/                        # 测试代码
├── docs/
│   └── API/
│       └── M3-内容管理模块API文档.md
├── pom.xml                          # Maven配置
└── README.md
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- PostgreSQL 12+

### 数据库配置

1. 创建PostgreSQL数据库：

```sql
CREATE DATABASE open436;
```

2. 执行数据库表结构脚本：

```bash
psql -U postgres -d open436 -f src/main/resources/db/schema.sql
```

3. 修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/open436
    username: your_username
    password: your_password
```

### 运行项目

1. 编译项目：

```bash
mvn clean package
```

2. 运行应用：

```bash
mvn spring-boot:run
```

或者直接运行：

```bash
java -jar target/Open436-Content-0.0.1-SNAPSHOT.jar
```

3. 访问Swagger UI：

```
http://localhost:8080/swagger-ui.html
```

## API文档

详细的API接口文档请查看：[M3-内容管理模块API文档](../docs/API/M3-内容管理模块API文档.md)

### 主要接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发布新帖 | POST | /api/content/posts | 用户发布新帖子 |
| 查询帖子列表 | GET | /api/content/posts | 分页查询帖子列表 |
| 查看帖子详情 | GET | /api/content/posts/{id} | 查看指定帖子详情 |
| 编辑帖子 | PUT | /api/content/posts/{id} | 编辑指定帖子 |
| 删除帖子 | DELETE | /api/content/posts/{id} | 删除指定帖子 |
| 查询用户帖子 | GET | /api/content/posts/user/{userId} | 查询用户的帖子列表 |
| 置顶帖子 | PUT | /api/content/posts/manage/{id}/pin | 置顶帖子（管理员） |
| 取消置顶 | DELETE | /api/content/posts/manage/{id}/pin | 取消置顶（管理员） |
| 恢复帖子 | POST | /api/content/posts/manage/{id}/restore | 恢复已删除的帖子（管理员） |
| 永久删除 | DELETE | /api/content/posts/manage/{id}/permanent | 硬删除帖子（管理员） |
| 编辑历史 | GET | /api/content/posts/manage/{id}/edit-history | 查看编辑历史（管理员） |

## 开发说明

### 当前状态

本项目目前处于**框架搭建阶段**，已完成：

- ✅ Maven依赖配置
- ✅ 数据库表结构设计
- ✅ JPA实体类
- ✅ DTO和VO类
- ✅ Repository接口
- ✅ Service接口和空实现
- ✅ Controller和Swagger注解
- ✅ 统一响应格式和异常处理
- ✅ Swagger API文档

### 待实现功能

所有Service实现类中的业务逻辑均为空方法，抛出 `UnsupportedOperationException("功能待实现")`。

需要实现的功能包括：

1. **PostServiceImpl**：
   - 发布帖子
   - 查询帖子列表
   - 查看帖子详情（含浏览量统计）
   - 编辑帖子（含编辑历史保存）
   - 删除帖子
   - 查询用户帖子列表

2. **PostManageServiceImpl**：
   - 置顶/取消置顶帖子
   - 恢复已删除的帖子
   - 硬删除帖子
   - 查看编辑历史

### 权限验证

当前版本使用临时的请求头方式传递用户信息：

- `X-User-Id`: 用户ID
- `X-Is-Admin`: 是否为管理员

**注意**：这是临时方案，实际应该集成Spring Security + JWT进行身份认证和权限验证。

## 业务规则

### 发布帖子

- 标题长度：5-100个字符
- 内容长度：10-50000个字符
- 必须选择有效的板块

### 编辑帖子

- 仅作者本人或管理员可编辑
- 发布后24小时内可无限制编辑
- 24小时后每篇帖子最多编辑5次（管理员无限制）
- 每次编辑保存历史版本

### 删除帖子

- 仅作者本人或管理员可删除
- 普通用户执行软删除（可恢复）
- 管理员可选择软删除或硬删除

### 置顶帖子

- 仅管理员可操作
- 全局置顶最多3篇
- 每个板块置顶最多5篇

### 浏览量统计

- 每次访问浏览量+1
- 同一用户10分钟内多次访问不重复计数
- 作者本人访问不计入浏览量

## 数据库表

### post - 帖子主表

存储帖子的基本信息、状态、统计数据等。

### post_edit_history - 帖子编辑历史表

记录帖子的每次编辑历史。

### post_view_record - 帖子浏览记录表

用于去重统计浏览量。

详细表结构请查看：`src/main/resources/db/schema.sql`

## 依赖模块

本模块依赖以下模块：

- **M1 - 权限认证模块**：验证用户身份和权限
- **M2 - 用户管理模块**：获取作者信息
- **M5 - 板块管理模块**：验证板块是否存在
- **M7 - 文件管理模块**：上传帖子中的图片

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 联系方式

- 项目主页：https://github.com/open436
- 问题反馈：https://github.com/open436/issues
- 开发团队：dev@open436.com

---

**版本**：v1.0.0  
**最后更新**：2025-11-03  
**维护状态**：活跃开发中

