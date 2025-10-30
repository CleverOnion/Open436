package com.open436.auth.config;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Consul 配置类
 */
@Configuration
public class ConsulConfig {
    
    @Value("${spring.cloud.consul.host}")
    private String consulHost;
    
    @Value("${spring.cloud.consul.port}")
    private int consulPort;
    
    /**
     * Consul 客户端 Bean
     */
    @Bean
    public ConsulClient consulClient() {
        return new ConsulClient(consulHost, consulPort);
    }
    
    /**
     * RestTemplate Bean（用于服务间调用）
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

