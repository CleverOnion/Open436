package com.open436.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class Open436AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(Open436AuthApplication.class, args);
    }

}
