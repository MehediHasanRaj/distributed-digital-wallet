# 05 API Gateway & Routing

## We will build

- ✅ Dynamic Routing
- ✅ Eureka Integration
- ✅ Load Balancing
- ✅ Global Filters
- ✅ Logging
- ✅ Correlation IDs
- ✅ CORS
- ✅ Request Validation

---

# Learning Objectives

After Repository 05, you'll understand:

- Why Gateway exists
- Routing
- Predicates
- Filters
- Global Filters
- Cross-Cutting Concerns
- Correlation IDs
- CORS
- Gateway Security (later)

---

# Responsibilities

Gateway should do the following **and no business logic**:

- ✅ Route
- ✅ Filter
- ✅ Authenticate (later)
- ✅ Logging
- ✅ Metrics
- ✅ Rate Limiting

---

# Cross Cutting Concerns

Huge interview topic.

Every service needs:

- Logging
- Authentication
- Authorization
- Rate Limiting
- Metrics
- Tracing
- Headers
- Compression

### Should every service implement these?

**No. Gateway should handle them.**

### Common Interview Question

**Why API Gateway?**

Provides a single entry point, centralizes cross-cutting concerns, and hides internal service topology.

**What are Cross-Cutting Concerns?**

Concerns common to many services, such as:

- Logging
- Authentication
- Metrics
- Rate Limiting

---

# Lesson 1: Build Spring Cloud Gateway

Create a simple project like the others with only:

- Spring Cloud Gateway
- Eureka Discovery Client
- Spring Boot Actuator

Nothing else.

---

## Understanding `application.properties`

### Enable Discovery Locator

```properties
spring.cloud.gateway.discovery.locator.enabled=true
```

This tells Gateway to automatically discover every Eureka service without writing routing code.

If `wallet-service` is registered in Eureka, Gateway automatically creates:

```text
/wallet-service/**
```

dynamic routes.

Later we'll polish these routes.

---

## Run Order

```text
Eureka
    ↓
Other Services
    ↓
API Gateway
```

Now all requests can go through the Gateway port.

---

# Lesson 2 – Explicit Routing (Enterprise Style)

## Goal

Replace Discovery Locator with explicit routes using Java configuration and expose clean public APIs.

---

## Step 1

Disable auto discovery.

```properties
spring.cloud.gateway.discovery.locator.enabled=false
```

Now Gateway will **not** create routes automatically.

Instead of exposing:

```text
localhost:8080/wallet-service/api/v1/users/**
```

we'll expose clean URLs.

---

## Step 2 — Create Route Configuration

Create a configuration class inside the `config` package.

```java
@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {

        return builder.routes()

                // Identity Service
                .route("identity-service", route -> route
                        .path("/api/v1/users/**")
                        .uri("lb://identity-service"))

                // Wallet Service
                .route("wallet-service", route -> route
                        .path("/api/v1/wallets/**")
                        .uri("lb://wallet-service"))

                .build();
    }
}
```

---

## Step 3

Now test:

Instead of

```text
http://localhost:8082/api/v1/users/{id}
```

Call

```text
http://localhost:8080/api/v1/users/{id}
```

---

# Route Predicates

Route predicates determine whether a request matches a route.

Available predicates:

```java
.host()
.method()
.header()
.cookie()
.query()
.remoteAddr()
.after()
.before()
.between()
```

Example:

Only allow **POST** requests.

```java
.method(HttpMethod.POST)
```

Only allow requests from:

```java
.remoteAddr("192.168.1.0/24")
```

---

# Why I Prefer Java Configuration

- Compile-time safety
- IDE refactoring
- Easier debugging
- Conditional routes
- Better for complex logic

---

# Interview Questions

## 1. Why use explicit routes?

Explicit routes provide full control over:

- Exposed APIs
- Security
- Filters
- Versioning
- URL design

---

## 2. What is a Route Predicate?

A condition that determines whether a request matches a route, such as:

- Path
- Method
- Header
- Host

---

## 3. Does Gateway know service IP addresses?

No.

Gateway uses logical service names.

Spring Cloud LoadBalancer and Eureka resolve the actual instances.

---

## 4. Why not expose `/identity-service/...`?

Because it leaks internal architecture.

Public APIs should be stable and business-focused, independent of internal service names.

---

# Lesson 3 – Global Filters & Correlation IDs

## Goal

Implement a **Global Filter** that:

- Generates and propagates Correlation IDs
- Logs every request
- Measures request execution time

---

# Why do we need Correlation IDs?

Imagine one request travels through multiple microservices.

```
Gateway
   ↓
Identity
   ↓
Wallet
   ↓
Notification
   ↓
Database
```

If one service fails, tracing the request becomes difficult.

A **Correlation ID** solves this problem.

Example:

```text
X-Correlation-Id: 8b24f63d-66d5-4f97-a832-b98b35d2d970
```

Every service logs the same ID.

---

# Step 1 — Create Constants

```java
public final class Headers {

    private Headers() {}

    public static final String CORRELATION_ID =
            "X-Correlation-Id";
}
```

### Why?

Instead of writing:

```java
"X-Correlation-Id"
```

everywhere, use:

```java
Headers.CORRELATION_ID
```

This avoids typo mistakes.

---

# Step 2 — Create Global Filter

```java
@Slf4j
@Component
public class CorrelationIdFilter
        implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String correlationId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(Headers.CORRELATION_ID);

        // if not exist, generate one
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        String finalCorrelationId = correlationId;

        ServerWebExchange mutatedExchange =
                exchange.mutate()
                        .request(builder ->
                                builder.header(
                                        Headers.CORRELATION_ID,
                                        finalCorrelationId))
                        .build();

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
```

---

# What happens?

```
Incoming Request
        │
        ▼
Header Exists?
        │
   ┌────┴────┐
   │         │
  Yes        No
   │         │
   │    Generate UUID
   │         │
   └────┬────┘
        ▼
Attach Header
        ▼
Forward Request
```

---

# Why `Ordered.HIGHEST_PRECEDENCE`?

Many filters exist inside Spring Cloud Gateway.

We want this filter to execute **before every other filter** so every request already contains the Correlation ID.

---

# Step 3 — Request Logging

Record request start time.

```java
long start = System.currentTimeMillis();
```

Then log after the request completes.

```java
return chain.filter(mutatedExchange).then(
    Mono.fromRunnable(() -> {

        long duration =
                System.currentTimeMillis() - start;

        log.info(
                "[{}] {} {} {} ms",
                finalCorrelationId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI(),
                duration);

    })
);
```

---

# Why Global Filter?

Adding logging inside every controller is terrible.

Instead:

```
Gateway
    │
    ▼
One Global Filter
    │
    ▼
Everything is logged
```

Centralized logging is much cleaner.

---

# Filter Execution

```
Incoming Request
        │
        ▼
Global Filter
        │
        ▼
Wallet Service
        │
        ▼
Response
        │
        ▼
Global Filter
        │
        ▼
Client
```

A filter can execute:

- Before the request
- After the response

This makes Global Filters extremely powerful.

---

# Why Ordered?

Ordered determines **who runs first**.

Higher precedence executes first.

---

# Real Production Filter Chain

```
Authentication Filter
        ↓
Correlation Filter
        ↓
Logging Filter
        ↓
Metrics Filter
        ↓
Rate Limiter
```

---

# Common Mistakes

❌ Generate a new UUID in every service.

Use **one request → one Correlation ID**.

---

❌ Controller logging everywhere.

Gateway logging is better.

---

❌ Random header names.

Always use constants.

---

❌ Forgetting response time.

Latency is an important production metric.

---

# Interview Questions

## 1. What is a Correlation ID?

A unique identifier attached to a request that allows the same request to be traced across multiple services.

---

## 2. Why generate it in Gateway?

Gateway is the entry point into the system.

Every downstream service receives the same identifier.

---

## 3. Why use a Global Filter?

To avoid duplicate logging and centralize request processing.

---

## 4. Why log request duration?

To measure latency.

Useful for:

- Performance analysis
- Monitoring
- Troubleshooting

---

## 5. What happens if a request goes through five services?

Every service should log **the same Correlation ID**, making the request easy to trace end-to-end.

---

# Lesson 4 – Gateway Filters Deep Dive

## Goal

Master:

- Global Filters
- Route Filters
- Filter execution order
- Pre vs Post filters
- Path rewriting
- Header manipulation
- Request/Response mutation
- Custom Gateway filters

---

# Learning Objectives

By the end of this lesson you'll understand:

- Global Filters
- Route Filters
- Filter execution order
- Pre vs Post filters
- Path rewriting
- Header manipulation
- Request/Response mutation
- Custom Gateway filters
- Enterprise filter chain

---

# Gateway Processing Pipeline

Every request goes through this pipeline.

```text
Incoming Request
        │
        ▼
Global Pre Filters
        │
        ▼
Route Predicate
        │
        ▼
Route Filters
        │
        ▼
Downstream Service
        │
        ▼
Route Post Filters
        │
        ▼
Global Post Filters
        │
        ▼
Response
```

---

# Two Types of Filters

## 1. Global Filter

Runs for **every request**.

Examples:

- `/api/v1/users/**`
- `/api/v1/wallets/**`
- `/api/v1/orders/**`

Typically used for:

- Correlation ID
- Logging
- Metrics
- Authentication
- Rate Limiting

---

## 2. Route Filter

Runs **only for one specific route**.

Example:

```
/api/v1/users/**
```

may need user validation.

While

```
/api/v1/wallets/**
```

may require a currency header.

---

# Global Filter vs Route Filter

| Global Filter | Route Filter |
|---------------|--------------|
| Runs for every request | Runs only on matching routes |
| Cross-cutting concerns | Route-specific logic |
| Logging | Rewrite path |
| Metrics | Add headers |
| Authentication | Custom validation |

---

# Pre Filters

A **Pre Filter** runs before the request reaches the downstream service.

Examples:

- Check JWT
- Generate Correlation ID
- Validation check

```text
Gateway
    │
Pre Filter
    │
    ▼
Service
```

---

# Post Filters

A **Post Filter** runs after the downstream service returns.

Examples:

- Measure execution time
- Add response headers
- Audit logging

```text
Service
    │
Post Filter
    │
    ▼
Client
```

---

# Custom Route Filter

## Step 1 — Create Route Filter

```java
@Slf4j
@Component
public class RequestValidationFilter {

    public GatewayFilter validateRequest() {

        return (exchange, chain) -> {

            log.info(
                    "Validating request {}",
                    exchange.getRequest().getURI());

            return chain.filter(exchange);

        };
    }

}
```

---

## Step 2 — Add the Filter to the Route

```java
// Wallet Service
.route("wallet-service", route -> route
        .path("/api/v1/wallets/**")
        .filters(filter -> filter
                .filter(
                        requestValidationFilter
                                .validateRequest()))
        .uri("lb://wallet-service"))
```

Notice that we only added:

```java
.filters(filter -> filter
        .filter(requestValidationFilter.validateRequest()))
```

inside the route configuration.

---

# Result

Identity Route

```
No validation filter
```

Wallet Route

```
Validation Filter
```

Each route can have its own behavior.

---

# Filter Execution Order

Suppose we have:

```
Global Filter A

Global Filter B

Route Filter

Service
```

Execution order becomes:

```text
A (Pre)
    ↓
B (Pre)
    ↓
Route Filter
    ↓
Service
    ↓
Route Filter (Post)
    ↓
B (Post)
    ↓
A (Post)
```

Think of it like a **Stack (LIFO)**.

The last filter entered is the first to finish on the response path.

---

# Enterprise Filter Chain

A typical production Gateway pipeline looks like this:

```text
Incoming Request
        │
        ▼
Correlation ID
        ▼
Authentication
        ▼
Rate Limiter
        ▼
Logging
        ▼
Metrics
        ▼
Header Validation
        ▼
Routing
        ▼
Downstream Service
        ▼
Response Metrics
        ▼
Audit Logging
        ▼
Client
```

---

# Common Mistakes

❌ Business logic inside filters.

Filters should handle infrastructure concerns, **not business logic**.

---

❌ Logging sensitive headers.

Never log:

- Passwords
- JWT secrets
- API Keys
- Tokens

---

❌ Putting route-specific logic into Global Filters.

Keep route-specific behavior inside Route Filters.

---

❌ Overusing path rewriting.

Rewrite paths only when necessary.

---

# Interview Questions

## 1. Difference between Global Filter and Route Filter?

Global Filters apply to every request.

Route Filters apply only to requests matching a specific route.

---

## 2. What is a Pre Filter?

A filter that executes **before** the request reaches the downstream service.

---

## 3. What is a Post Filter?

A filter that executes **after** the downstream service returns a response but **before** the response is sent to the client.

---

## 4. Why rewrite paths?

To decouple external APIs from internal service endpoints, allowing internal implementations to change without affecting clients.

---

# Lesson 5 – CORS, Rate Limiting & Production Gateway Best Practices

## Goal

Configure CORS correctly, understand browser preflight requests, implement gateway-level rate limiting, and learn production API Gateway best practices.

---

# Understanding CORS

**CORS (Cross-Origin Resource Sharing)** is a browser security mechanism that restricts cross-origin HTTP requests unless the server explicitly allows them.

A browser considers the frontend and backend to be **different origins**, so it blocks requests by default to protect users.

---

# What is a Preflight Request?

Before sending certain requests (such as **POST**, **PUT**, or **DELETE**), the browser first sends an **OPTIONS** request.

The Gateway replies with:

- `200 OK`
- `Access-Control-Allow-Origin`
- `Access-Control-Allow-Methods`
- `Access-Control-Allow-Headers`
- Other CORS headers

If the response allows it, the browser then sends the actual request.

---

# Step 1 – Global CORS Configuration

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':

            allowedOrigins:
              - http://localhost:3000

            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS

            allowedHeaders:
              - "*"

            allowCredentials: true
```

Now the React application can call the Gateway successfully.

---

# Production Note

❌ Don't use:

```yaml
allowedOrigins: "*"
```

in production.

Instead, explicitly specify the allowed frontend domains.

---

# Part 2 – Rate Limiting

Imagine someone sends **100,000 requests per second**.

Your Wallet Service may crash.

The solution is **Rate Limiting**.

---

# Architecture

```text
Client
   │
   ▼
Gateway
   │
   ▼
Rate Limiter
   │
   ▼
Wallet Service
```

---

# Common Rate Limiting Algorithms

### Fixed Window

- Very simple
- Easy to implement
- Less accurate around window boundaries

---

### Sliding Window

- More accurate
- Production favorite
- Smooth request distribution

---

### Token Bucket

- Most common
- Excellent for APIs
- Allows short traffic bursts while enforcing limits

---

Spring Cloud Gateway supports:

```text
RedisRateLimiter
```

---

# Complete Request Flow

```text
Browser
    │
    ▼
CORS Check
    │
    ▼
Gateway
    │
    ▼
Rate Limiter
    │
    ▼
Authentication
    │
    ▼
Logging
    │
    ▼
Routing
    │
    ▼
Microservices
    │
    ▼
Response
    │
    ▼
Gateway
    │
    ▼
Browser
```

---

# Production Best Practices

- Keep the Gateway stateless.
- Centralize cross-cutting concerns.
- Version public APIs.
- Never expose internal service names.
- Use HTTPS.
- Log Correlation IDs.
- Monitor latency and request metrics.
- Apply rate limiting at the Gateway.
- Authenticate requests before routing.
- Keep business logic out of the Gateway.

---

# Common Mistakes

❌ Allowing every origin.

Never use:

```yaml
allowedOrigins: "*"
```

in production.

---

❌ Rate limiting inside every service.

The Gateway is the correct place.

---

❌ Breaking APIs.

Always version your APIs.

Example:

```text
/api/v1/users
/api/v2/users
```

---

❌ Stateful Gateway.

Avoid sessions.

Prefer stateless authentication using JWT.

---

❌ Logging secrets.

Never log:

- Passwords
- Tokens
- API Keys
- Secrets

---

# Interview Questions

## 1. What is CORS?

A browser security mechanism that restricts cross-origin HTTP requests unless the server explicitly allows them.

---

## 2. Why does the browser send an OPTIONS request?

To perform a **preflight check** and verify whether the actual request is permitted.

---

## 3. Why implement rate limiting at the Gateway?

Because the Gateway is the first entry point and can protect downstream services from excessive traffic before requests reach them.

---

## 4. Why is Redis commonly used for rate limiting?

Redis provides a shared, fast, in-memory data store, allowing rate limits to remain consistent across multiple Gateway instances.

---

## 5. Why version APIs?

To introduce breaking changes without disrupting existing clients.

Example:

```text
/api/v1/users
/api/v2/users
```

---

## 6. Why should the Gateway remain stateless?

Stateless services:

- Scale more easily
- Simplify load balancing
- Work well with token-based authentication (such as JWT)

---

# Repository 05 Summary

After completing this repository, you should understand:

- ✅ Dynamic Routing
- ✅ Eureka Integration
- ✅ Explicit Routing
- ✅ Route Predicates
- ✅ Global Filters
- ✅ Route Filters
- ✅ Correlation IDs
- ✅ Request Logging
- ✅ Filter Execution Order
- ✅ Request Validation
- ✅ CORS
- ✅ Browser Preflight Requests
- ✅ Gateway Rate Limiting
- ✅ Redis Rate Limiter
- ✅ Production Gateway Architecture
- ✅ Enterprise Best Practices

---

# Common Interview Topics

- Why use an API Gateway?
- What are Cross-Cutting Concerns?
- Global Filter vs Route Filter
- Correlation ID
- Filter Execution Order
- Pre vs Post Filters
- Route Predicates
- CORS
- Browser Preflight Requests
- Rate Limiting
- Token Bucket vs Sliding Window
- Why Redis?
- Why Stateless Gateway?
- Why Version APIs?
- Production Gateway Best Practices

---
