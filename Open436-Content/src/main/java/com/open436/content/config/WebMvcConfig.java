package com.open436.content.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 用于配置静态资源路径（包括 Swagger UI）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    /**
     * 配置静态资源处理器
     * 确保 Swagger UI 资源可以正确访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Springdoc 会自动处理 Swagger UI 资源，通常不需要手动配置
        // 但如果出现资源访问问题，可以取消下面的注释
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/")
                .resourceChain(false);
        
        // 支持简化路径 /swagger-ui.html
        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/")
                .resourceChain(false);
    }
}

