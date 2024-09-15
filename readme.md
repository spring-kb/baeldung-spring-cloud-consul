A Quick Guide to Spring Cloud Consul
====================================
> Source: https://www.baeldung.com/spring-cloud-consul

**1\. Overview**
----------------

The [Spring Cloud Consul](https://cloud.spring.io/spring-cloud-consul/) project provides easy integration with Consul for Spring Boot applications.

[Consul](https://www.consul.io/intro/) is a tool that provides components for resolving some of the most common challenges in a micro-services architecture:

*   Service Discovery – to automatically register and unregister the network locations of service instances
*   Health Checking – to detect when a service instance is up and running
*   Distributed Configuration – to ensure all service instances use the same configuration

In this article, we’ll see how we can configure a Spring Boot application to use these features.

**2\. Prerequisites**
---------------------

To start with, it’s recommended to take a quick look at [Consul](https://www.consul.io/intro/) and all its features.

In this article, we’re going to use a Consul agent running on _localhost:8500_. For more details about how to install Consul and run an agent, refer to this [link](https://developer.hashicorp.com/consul/docs/install).

freestar.config.enabled\_slots.push({ placementName: "baeldung\_leaderboard\_mid\_1", slotId: "baeldung\_leaderboard\_mid\_1" });

First, we’ll need to add the [spring-cloud-starter-consul-all](https://mvnrepository.com/search?q=spring-cloud-starter-consul-all) dependency to our _pom.xml_:

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-consul-all</artifactId>
        <version>3.1.1</version>
    </dependency>

**3\. Service Discovery**
-------------------------

Let’s write our first Spring Boot application and wire up with the running Consul agent:

    @SpringBootApplication
    public class ServiceDiscoveryApplication {
    
        public static void main(String[] args) {
            new SpringApplicationBuilder(ServiceDiscoveryApplication.class)
              .web(true).run(args);
        }
    }

**By default, Spring Boot will try to connect to the Consul agent at _localhost:8500_.** To use other settings, we need to update the _application.yml_ file:

    spring:
      cloud:
        consul:
          host: localhost
          port: 8500

Then, if we visit the Consul agent’s site in the browser at _http://localhost:8500_, we’ll see that our application was properly registered in Consul with the identifier from _“${spring.application.name}:${profiles separated by comma}:${server.port}”_.

To customize this identifier, we need to update the property _spring.cloud.discovery.instanceId_ with another expression:

freestar.config.enabled\_slots.push({ placementName: "baeldung\_leaderboard\_mid\_2", slotId: "baeldung\_leaderboard\_mid\_2" });

    spring:
      application:
        name: myApp
      cloud:
        consul:
          discovery:
            instanceId: ${spring.application.name}:${random.value}

If we run the application again, we’ll see that it was registered using the identifier _“MyApp”_ plus a random value. We need this for running multiple instances of our application on our local machine.

Finally, **to disable Service Discovery, we need to set the property _spring.cloud.consul.discovery.enabled_ to _false_.**

### **3.1. Looking Up Services**

We already have our application registered in Consul, but how can clients find the service endpoints? We need a discovery client service to get a running and available service from Consul.

**Spring provides a _DiscoveryClient API_ for this**, which we can enable with the _@EnableDiscoveryClient_ annotation:

    @SpringBootApplication
    @EnableDiscoveryClient
    public class DiscoveryClientApplication {
        // ...
    }

Then, we can inject the _DiscoveryClient_ bean into our controller and access the instances:

freestar.config.enabled\_slots.push({ placementName: "baeldung\_leaderboard\_mid\_3", slotId: "baeldung\_leaderboard\_mid\_3" });

    @RestController
    public class DiscoveryClientController {
     
        @Autowired
        private DiscoveryClient discoveryClient;
    
        public Optional<URI> serviceUrl() {
            return discoveryClient.getInstances("myApp")
              .stream()
              .findFirst() 
              .map(si -> si.getUri());
        }
    }

Finally, we’ll define our application endpoints:

    @GetMapping("/discoveryClient")
    public String discoveryPing() throws RestClientException, 
      ServiceUnavailableException {
        URI service = serviceUrl()
          .map(s -> s.resolve("/ping"))
          .orElseThrow(ServiceUnavailableException::new);
        return restTemplate.getForEntity(service, String.class)
          .getBody();
    }
    
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

The _“myApp/ping”_ path is the Spring application name with the service endpoint. Consul will provide all available applications named _“myApp”._

**4\. Health Checking**
-----------------------

Consul checks the health of the service endpoints periodically.

By default, **Spring implements the health endpoint to return _200 OK_ if the app is up**. If we want to customize the endpoint we have to update the _application.yml:_

    spring:
      cloud:
        consul:
          discovery:
            healthCheckPath: /my-health-check
            healthCheckInterval: 20s

As a result, Consul will poll the _“/my-health-check”_ endpoint every 20 seconds.

Let’s define our custom health check service to return a _FORBIDDEN_ status:

    @GetMapping("/my-health-check")
    public ResponseEntity<String> myCustomCheck() {
        String message = "Testing my healh check function";
        return new ResponseEntity<>(message, HttpStatus.FORBIDDEN);
    }

If we go to the Consul agent site, we’ll see that our application is failing. To fix this, the _“/my-health-check”_ service should return the HTTP _200 OK_ status code.

**5\. Distributed Configuration**
---------------------------------

This feature **allows synchronizing the configuration among all the services**. Consul will watch for any configuration changes and then trigger the update of all the services.

freestar.config.enabled\_slots.push({ placementName: "baeldung\_incontent\_1", slotId: "baeldung\_incontent\_1" });

First, we need to add the [spring-cloud-starter-consul-config](https://mvnrepository.com/search?q=a:spring-cloud-starter-consul-config) dependency to our _pom.xml_:

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-consul-config</artifactId>
        <version>3.1.1</version>
    </dependency>

We also need to move the settings of Consul and Spring application name from the _application.yml_ file to the _bootstrap.yml_ file which Spring loads first.

Then, we need to enable Spring Cloud Consul Config:

    spring:
      application:
        name: myApp
      cloud:
        consul:
          host: localhost
          port: 8500
          config:
            enabled: true

Spring Cloud Consul Config will look for the properties in Consul at _“/config/myApp”_. So if we have a property called _“my.prop”_, we would need to create this property in the Consul agent site.

We can create the property by going to the _“KEY/VALUE”_ section, then entering _“/config/myApp/my/prop”_ in the _“Create Key”_ form and _“Hello World”_ as value. Finally, click the _“Create”_ button.

Bear in mind that if we are using Spring profiles, we need to append the profiles next to the Spring application name. For example, if we are using the _dev_ profile, the final path in Consul will be _“/config/myApp,dev”._

Now, let’s see what our controller with the injected properties looks like:

    @RestController
    public class DistributedPropertiesController {
    
        @Value("${my.prop}")
        String value;
    
        @Autowired
        private MyProperties properties;
    
        @GetMapping("/getConfigFromValue")
        public String getConfigFromValue() {
            return value;
        }
    
        @GetMapping("/getConfigFromProperty")
        public String getConfigFromProperty() {
            return properties.getProp();
        }
    }

And the _MyProperties_ class:

freestar.config.enabled\_slots.push({ placementName: "baeldung\_incontent\_2", slotId: "baeldung\_incontent\_2" });

    @RefreshScope
    @Configuration
    @ConfigurationProperties("my")
    public class MyProperties {
        private String prop;
    
        // standard getter, setter
    }

If we run the application, the field _value_ and _properties_ have the same _“Hello World”_ value from Consul.

### **5.1. Updating the Configuration**

What about updating the configuration without restarting the Spring Boot application?

If we go back to the Consul agent site and we update the property _“/config/myApp/my/prop”_ with another value like _“New Hello World”_, then the field _value_ will not change and the field _properties_ will have been updated to _“New Hello World”_ as expected.

This is because the field _properties_ is a _MyProperties_ class has the _@RefreshScope_ annotation. **All beans annotated with the _@RefreshScope_ annotation will be refreshed after configuration changes.**

In real life, we should not have the properties directly in Consul, but we should store them persistently somewhere. We can do this using a [Config Server](/spring-cloud-configuration).

**6\. Conclusion**
------------------

In this article, we’ve seen how to set up our Spring Boot applications to work with Consul for Service Discovery purposes, customize the health checking rules and share a distributed configuration.

We’ve also introduced a number of approaches for the clients to invoke these registered services.

As usual, sources can be found [over on GitHub](https://github.com/eugenp/tutorials/tree/master/spring-cloud-modules/spring-cloud-consul).

# Run and Test
## export consul config
```bash
cd docker/consul/
docker exec -d consul consul kv export config/ > consul-kv-docker.json
```
## Run
docker compose up

cd ./my-service
./mvnw clean spring-boot:run
./mvnw clean package spring-boot:repackage

cd ./discovery-client-service
./mvnw clean spring-boot:run
./mvnw clean package spring-boot:repackage

## Test
curl http://localhost:8080/ping

curl http://localhost:9090/discoveryClient

curl http://localhost:8080/getConfigFromValue

curl http://localhost:8080/getConfigFromProperty

Consul
http://localhost:8500/