package com.open436.auth.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpInterface;
import com.open436.auth.service.PermissionService;
import com.open436.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Sa-Token 配置类
 * 配置 Sa-Token 拦截器和权限接口
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {
    
    private final PermissionService permissionService;
    private final RoleService roleService;
    
    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解式鉴权功能
        registry.addInterceptor(new SaInterceptor())
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/api/auth/login",      // 登录接口
                "/error"                // 错误页面
            );
    }
    
    /**
     * 权限认证接口实现
     */
    @Bean
    public StpInterface stpInterface() {
        return new StpInterface() {
            
            /**
             * 返回指定用户的权限列表
             */
            @Override
            public List<String> getPermissionList(Object loginId, String loginType) {
                Long userId = Long.parseLong(loginId.toString());
                return permissionService.getUserPermissionCodes(userId);
            }
            
            /**
             * 返回指定用户的角色列表
             */
            @Override
            public List<String> getRoleList(Object loginId, String loginType) {
                Long userId = Long.parseLong(loginId.toString());
                return roleService.getUserRoleCodes(userId);
            }
        };
    }
}


