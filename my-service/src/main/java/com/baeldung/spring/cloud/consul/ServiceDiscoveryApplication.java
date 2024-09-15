package com.baeldung.spring.cloud.consul;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ServiceDiscoveryApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ServiceDiscoveryApplication.class)
                .web(WebApplicationType.SERVLET).run(args);
    }
}