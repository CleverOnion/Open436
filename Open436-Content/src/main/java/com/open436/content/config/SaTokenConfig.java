package com.open436.content.config;

// import cn.dev33.satoken.interceptor.SaInterceptor; // 暂时未使用，启用拦截器时需要取消注释
import cn.dev33.satoken.stp.StpInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 配置类
 * 配置 Sa-Token 拦截器和权限接口
 * 
 * 注意：Content服务不直接管理用户权限，权限信息通过Kong网关传递
 * 这里只做基本的登录拦截，权限验证通过网关完成
 */
@Slf4j
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    
    /**
     * 注册 Sa-Token 拦截器
     * 配置需要认证的路径和排除的路径
     * 
     * 注意：暂时禁用拦截器，不强制要求登录验证
     * 后续需要启用登录验证时，可以取消注释下面的代码
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // 暂时禁用拦截器，不强制要求登录验证
        // 后续需要启用时，取消下面的注释
        // 注意：即使启用拦截器，也要排除Swagger相关路径
        /*
        registry.addInterceptor(new SaInterceptor())
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/swagger-ui/**",           // Swagger UI
                "/swagger-ui.html",         // Swagger UI (简化路径)
                "/swagger-ui/index.html",   // Swagger UI (默认路径)
                "/v3/api-docs/**",          // OpenAPI文档
                "/actuator/**",             // Actuator健康检查
                "/error",                   // 错误页面
                "/favicon.ico"              // 网站图标
            );
        */
    }
    
    /**
     * 权限认证接口实现
     * 注意：Content服务不直接查询权限数据库，权限信息通过网关传递
     * 这里返回空列表，实际权限验证由网关完成
     */
    @Bean
    public StpInterface stpInterface() {
        return new StpInterface() {
            
            /**
             * 返回指定用户的权限列表
             * Content服务不直接管理权限，返回空列表
             */
            @Override
            public List<String> getPermissionList(Object loginId, String loginType) {
                // 权限验证由Kong网关完成，这里返回空列表
                return new ArrayList<>();
            }
            
            /**
             * 返回指定用户的角色列表
             * 从Session中获取角色信息（由网关传递）
             */
            @Override
            public List<String> getRoleList(Object loginId, String loginType) {
                // 权限验证由Kong网关完成，这里返回空列表
                // 角色信息通过网关传递，由Sa-Token自动处理
                return new ArrayList<>();
            }
        };
    }
}

