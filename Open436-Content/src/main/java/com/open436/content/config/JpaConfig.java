package com.open436.content.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA配置类
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.open436.content.repository")
@EnableTransactionManagement
public class JpaConfig {
    // JPA相关配置在application.yml中已配置
    // 此类用于开启JPA Repository扫描和事务管理
}

