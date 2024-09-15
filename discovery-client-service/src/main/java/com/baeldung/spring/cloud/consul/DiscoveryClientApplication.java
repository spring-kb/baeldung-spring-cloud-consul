package com.baeldung.spring.cloud.consul;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DiscoveryClientApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(DiscoveryClientApplication.class)
                .web(WebApplicationType.SERVLET).run(args);
    }
}