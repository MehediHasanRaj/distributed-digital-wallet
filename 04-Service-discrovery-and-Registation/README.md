# 04 – Service Discovery & Registration

# Lesson 1: Theory

## Why do we need Service Discovery?

**Interview Question**

**Answer:**

Service instances change dynamically because microservices can be started, stopped, scaled, or moved to different servers. Hardcoding service URLs is not practical in a distributed system.

**Service Discovery** removes the need for hardcoded addresses by allowing services to automatically discover each other through a service registry.

---

## What is Eureka?

**Interview Question**

**Answer:**

**Eureka** is a **Service Registry** provided by Netflix and supported by Spring Cloud.

It allows microservices to:

- Register themselves automatically.
- Discover other services dynamically.
- Keep track of healthy service instances.

Instead of remembering IP addresses or ports, services simply ask Eureka where another service is running.

---

## What is Service Registration?

**Interview Question**

**Answer:**

**Service Registration** is the process where a microservice registers its:

- Network location (IP Address / Host)
- Port
- Service Name
- Metadata

with the Eureka Server.

Once registered, other services can discover and communicate with it.

---

## What is Heartbeat?

**Interview Question**

**Answer:**

A **Heartbeat** is a periodic signal sent by a service to Eureka indicating that it is still alive and healthy.

If Eureka does not receive heartbeats for a certain period, it assumes the instance is unavailable and removes it from the registry.

---

# Lesson 2 – Build Eureka Server

## Step 1 – Create a Spring Boot Project

Create a Spring Boot project with the **Eureka Server** dependency.

---

## Step 2 – Enable Eureka Server

Add the `@EnableEurekaServer` annotation to the main application class.

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}
```

---

## Step 3 – Configure `application.yml`

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false

  server:
    enable-self-preservation: true

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### Configuration Explanation

### `server.port: 8761`

Runs the Eureka Server on port **8761**.

---

### `spring.application.name`

```yaml
spring:
  application:
    name: eureka-server
```

Assigns the application name.

---

### `register-with-eureka: false`

```yaml
register-with-eureka: false
```

The Eureka Server **does not register itself** because it is the registry.

---

### `fetch-registry: false`

```yaml
fetch-registry: false
```

The Eureka Server does not fetch registry information because it already maintains it.

---

### `enable-self-preservation: true`

```yaml
enable-self-preservation: true
```

If there is a sudden network failure, Eureka **does not immediately remove registered services**.

This prevents accidental removal of healthy services during temporary network issues.

---

# Lesson 3 – Register Wallet Service with Eureka

## Step 1 – Add Eureka Client Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

This dependency allows the service to register itself with Eureka and discover other services.

---

## Step 2 – Configure `application.yml`

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

    register-with-eureka: true
    fetch-registry: true

  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${random.uuid}
```

### Configuration Explanation

### `defaultZone`

```yaml
defaultZone: http://localhost:8761/eureka/
```

Specifies the Eureka Server URL where the service will register itself.

---

### `register-with-eureka: true`

The service registers itself with Eureka.

---

### `fetch-registry: true`

Downloads the registry of all available services from Eureka.

This allows the service to discover other microservices.

---

### `prefer-ip-address: true`

Uses the IP address instead of the hostname when registering the service.

---

### `instance-id`

```yaml
instance-id: ${spring.application.name}:${random.uuid}
```

Each service instance receives a unique identifier.

Using `random.uuid` prevents duplicate instance IDs when running multiple instances of the same service.

---

## Step 3 – Enable Discovery Client

Add the `@EnableDiscoveryClient` annotation.

```java
@SpringBootApplication
@EnableDiscoveryClient
public class WalletServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletServiceApplication.class, args);
    }

}
```

---

## Common Mistakes

❌ Forgetting `spring.application.name`

Without it, the service cannot be registered with a proper name.

---

❌ Wrong `defaultZone`

The service cannot connect to Eureka if the URL is incorrect.

---

❌ Starting Wallet Service before Eureka Server

Always start the Eureka Server first.

---

❌ Using duplicate instance IDs

Running multiple instances with the same ID causes conflicts.

Use:

```yaml
instance-id: ${spring.application.name}:${random.uuid}
```

---

## Interview Question

### How does Feign locate another service without a URL?

**Answer:**

Feign delegates the request to **Spring Cloud LoadBalancer**.

The LoadBalancer queries **Eureka** for all healthy instances of the requested service, selects one (Round Robin by default), and then sends the HTTP request.

Flow:

```
Feign Client
      ↓
Spring Cloud LoadBalancer
      ↓
Eureka Server
      ↓
Healthy Service Instances
      ↓
Selected Instance
      ↓
HTTP Request
```

---

# Lesson 5 – Multiple Service Instances & Client-Side Load Balancing

Running multiple instances of the same microservice improves scalability and availability.

---

## Step 1 – Allow Port Override

```yaml
server:
  port: ${SERVER_PORT:8082}
```

### Explanation

- Default port is **8082**.
- At runtime, the port can be overridden using an environment variable.

Example:

```bash
SERVER_PORT=8083 mvn spring-boot:run
```

---

## Step 2 – Add Instance Information

```java
@Getter
@Component
public class InstanceInformation {

    @Value("${server.port}")
    private String port;

}
```

### Explanation

This component retrieves the current server port.

It is useful for verifying which instance processed the request.

---

## Step 3 – Add Logging

```java
log.info(
    "Identity Service [{}] processing user {}",
    instanceInformation.getPort(),
    saved.getUserId()
);
```

### Explanation

The log displays the port number of the service instance handling the request.

Example output:

```
Identity Service [8082] processing user 10
Identity Service [8083] processing user 11
Identity Service [8082] processing user 12
```

This helps verify that load balancing is distributing requests across multiple instances.

---

## Step 4 – Run Another Instance

Run another service instance on a different port.

```bash
SERVER_PORT=8083 mvn spring-boot:run
```

Now two service instances are running:

- Port 8082
- Port 8083

Spring Cloud LoadBalancer distributes incoming requests between them.

---

# What Happens When We Call

```java
identityFeignClient.getUser(id);
```

Execution flow:

```
Feign Client
      ↓
Spring Cloud LoadBalancer
      ↓
Eureka Server
      ↓
List of Healthy Service Instances
      ↓
Round Robin Algorithm
      ↓
Selected Service Instance
      ↓
HTTP Request
```

---

# Interview Questions

## What is Horizontal Scaling?

**Answer:**

Horizontal Scaling means running **multiple instances of the same service** to:

- Handle more traffic
- Improve availability
- Increase fault tolerance
- Distribute load across instances

Example:

```
Identity Service

Instance 1 → Port 8082

Instance 2 → Port 8083

Instance 3 → Port 8084
```

---

## How does Spring Cloud LoadBalancer choose an instance?

**Answer:**

By default, Spring Cloud LoadBalancer uses the **Round Robin** algorithm.

It cycles through the healthy service instances returned by Eureka, distributing requests evenly.

Example:

```
Request 1 → Instance A

Request 2 → Instance B

Request 3 → Instance C

Request 4 → Instance A

...
```

---

## Does Eureka perform Load Balancing?

**Answer:**

**No.**

Responsibilities are separated:

**Eureka**

- Registers services
- Stores service information
- Returns healthy service instances

**Spring Cloud LoadBalancer**

- Chooses which instance should handle the request
- Uses Round Robin by default (or other algorithms if configured)

Flow:

```
Eureka
    ↓
Returns all healthy instances

Spring Cloud LoadBalancer
    ↓
Selects one instance

Feign Client
    ↓
Makes the HTTP request
```
