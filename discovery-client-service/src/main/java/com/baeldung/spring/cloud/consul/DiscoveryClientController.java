package com.baeldung.spring.cloud.consul;

import java.net.URI;
import java.util.Optional;

import javax.naming.ServiceUnavailableException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
public class DiscoveryClientController {
    @Autowired
    private DiscoveryClient discoveryClient;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/discoveryClient")
    public String discoveryPing() throws RestClientException, ServiceUnavailableException {
        URI service = serviceUrl().map(s -> s.resolve("/ping"))
                .orElseThrow(ServiceUnavailableException::new);
        return restTemplate.getForEntity(service, String.class)
                .getBody();
    }

    public Optional<URI> serviceUrl() {
        return discoveryClient.getInstances("my-service")
                .stream()
                .findFirst()
                .map(si -> si.getUri());
    }
}
