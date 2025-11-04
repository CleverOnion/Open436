package com.open436.content.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI配置类
 */
@Configuration
public class SwaggerConfig {
    
    // Springdoc会自动扫描 @RestController 和 @Controller 注解的类
    // 无需手动配置包扫描
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .components(components());
        // 注意：不添加全局安全要求，允许接口在Swagger UI中直接测试
        // 如果需要认证，可以在Controller方法上单独添加@SecurityRequirement注解
    }
    
    /**
     * API基本信息
     */
    private Info apiInfo() {
        return new Info()
                .title("Open436 内容管理模块 API")
                .description("Open436论坛系统 - 内容管理模块接口文档\n\n" +
                        "本模块负责帖子的完整生命周期管理，包括：\n" +
                        "- 发布新帖\n" +
                        "- 浏览帖子列表\n" +
                        "- 查看帖子详情\n" +
                        "- 编辑帖子\n" +
                        "- 删除帖子\n" +
                        "- 置顶/取消置顶（管理员）\n" +
                        "- 查看编辑历史（管理员）\n\n" +
                        "**注意**: 当前版本为框架搭建阶段，具体业务逻辑待实现。")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Open436 开发团队")
                        .email("dev@open436.com")
                        .url("https://github.com/open436"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
    
    /**
     * 组件配置（包括安全方案）
     */
    private Components components() {
        return new Components()
                .addSecuritySchemes("Bearer-JWT", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("请在此处输入JWT Token，格式：Bearer {token}"))
                .addSecuritySchemes("X-User-Id", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-User-Id")
                        .description("临时方案：通过请求头传递用户ID（实际应该从JWT中解析）"));
    }
    
}

