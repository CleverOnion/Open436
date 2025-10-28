package com.open436.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 配置
 * 启用 JPA 审计功能（自动填充创建时间和更新时间）
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}

