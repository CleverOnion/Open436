package com.open436.auth.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.health.model.HealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Random;

/**
 * Consul 服务发现客户端
 * 用于从 Consul 发现服务并进行调用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConsulServiceClient {
    
    private final ConsulClient consulClient;
    private final RestTemplate restTemplate;
    private final Random random = new Random();
    
    /**
     * 发现服务实例
     * 
     * @param serviceName 服务名称
     * @return 服务URL
     */
    public String discoverService(String serviceName) {
        List<HealthService> services = consulClient
            .getHealthServices(serviceName, true, null)
            .getValue();
        
        if (services.isEmpty()) {
            throw new RuntimeException("Service not available: " + serviceName);
        }
        
        // 随机负载均衡
        HealthService service = services.get(random.nextInt(services.size()));
        String address = service.getService().getAddress();
        int port = service.getService().getPort();
        
        String serviceUrl = String.format("http://%s:%d", address, port);
        log.debug("Discovered service {}: {}", serviceName, serviceUrl);
        
        return serviceUrl;
    }
    
    /**
     * 调用其他服务
     * 
     * @param serviceName 服务名称
     * @param path API路径
     * @param responseType 响应类型
     * @return 响应对象
     */
    public <T> T callService(String serviceName, String path, Class<T> responseType) {
        String serviceUrl = discoverService(serviceName);
        String fullUrl = serviceUrl + path;
        
        log.debug("Calling service: {}", fullUrl);
        
        return restTemplate.getForObject(fullUrl, responseType);
    }
}

