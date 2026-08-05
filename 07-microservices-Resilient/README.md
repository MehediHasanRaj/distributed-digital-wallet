# Repository 07 – Microservices Resilience

> Complete study notes for building resilient microservices using **Resilience4j** in Spring Boot.

---

# Table of Contents

- [Lesson 1 – Why Microservices Fail](#lesson-1--why-microservices-fail)
- [Lesson 2 – Circuit Breaker](#lesson-2--circuit-breaker)
- [Lesson 3 – Retry Pattern](#lesson-3--retry-pattern)
- [Lesson 4 – Time Limiter](#lesson-4--time-limiter)
- [Lesson 5 – Bulkhead Pattern](#lesson-5--bulkhead-pattern)
- [Lesson 6 – Rate Limiter](#lesson-6--rate-limiter)
- [Lesson 7 – Combining Resilience Patterns](#lesson-7--combining-resilience-patterns)
- [Lesson 8 – Final Production Project](#lesson-8--final-project)

---

# Lesson 1 – Why Microservices Fail

## Goal

Understand why failures are **normal** in distributed systems and why resilience patterns are essential.

---

## Learning Objectives

After this lesson we will understand:

- Why distributed systems fail
- Cascading failures
- Partial failures
- Network latency
- Timeouts
- Retries
- Circuit Breakers
- Bulkheads
- Time Limiters
- Rate Limiters
- Fallbacks
- Why Resilience4j exists

---

## Types of Failures We Might Face

- Network failure
- Slow service
- Service crash
- Database failure
- Third-party failure
- Cascading failure *(one of the most dangerous)*

---

## Common Mistakes

### ❌ Infinite Retries

Creates retry storms.

### ❌ No Timeouts

Threads wait forever.

### ❌ No Circuit Breaker

Application keeps calling dead services.

### ❌ One Thread Pool

Everything blocks together.

### ❌ Returning Raw Exceptions

Return meaningful fallbacks where appropriate.

---

# Interview Questions

## 1. What is a cascading failure?

### Answer

A failure in one service causes resource exhaustion or delays in dependent services, leading to failures spreading throughout the system.

---

## 2. Why are retries dangerous?

### Answer

Uncontrolled retries can increase load on an already struggling service, creating a retry storm.

---

## 3. What problem does a Bulkhead solve?

### Answer

It isolates resources so failures in one area don't consume all threads or connections.

---

## 4. Why use a Circuit Breaker?

### Answer

To stop repeatedly calling an unhealthy service and fail fast until it recovers.

---

---

# Lesson 2 – Circuit Breaker (Theory + Complete Implementation)

## Goal

Prevent repeated calls to unhealthy services and fail fast until they recover.

---

## Step 1 – Add Dependency

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
```

---

## Step 2 – Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      identityService:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
```

---

## Step 3 – Service Implementation

Add the Circuit Breaker annotation on the method that calls another service.

```java
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    // OpenFeign Client
    private final IdentityFeignClient identityFeignClient;

    @CircuitBreaker(
        name = "identityService",   // Comes from application.yml
        fallbackMethod = "identityFallback"
    )

    // Method that calls Identity Service

}
```

---

## Circuit Breaker States

### 🟢 Closed

Requests are allowed.

---

### 🔴 Open

Requests fail immediately without calling the dependency.

---

### 🟡 Half-Open

A small number of requests are allowed to test whether the service has recovered.

---

## Common Mistakes

### ❌ Huge Sliding Window

Recovery becomes slow.

### ❌ Tiny Sliding Window

Circuit becomes unstable.

### ❌ Returning null in fallback

Always return a meaningful response or throw a domain-specific exception.

### ❌ Catching Exception manually

Let Circuit Breaker track failures automatically.

### ❌ One Circuit Breaker for every application

Create a separate Circuit Breaker for each downstream dependency.

---

# Interview Questions

## 1. What problem does a Circuit Breaker solve?

### Answer

It prevents repeated calls to an unhealthy dependency, reducing resource consumption and allowing faster failure handling.

---

## 2. Explain the three states.

### Answer

**Closed**

- Requests are allowed.

**Open**

- Requests fail immediately without calling the dependency.

**Half-Open**

- A limited number of test requests determine whether the dependency has recovered.

---

## 3. What is a sliding window?

### Answer

A recent set of requests used to calculate the current failure rate instead of considering the application's entire history.

---

## 4. Why use a fallback?

### Answer

To return a controlled response or graceful degradation instead of propagating low-level failures directly to clients.

---

## 5. Can a Circuit Breaker recover automatically?

### Answer

Yes.

After the configured wait duration, it transitions to the **Half-Open** state and tests whether the dependency is healthy again.

---

## 6. Should different downstream services share the same Circuit Breaker?

### Answer

No.

Each dependency should generally have its own Circuit Breaker because every downstream service has different failure characteristics.

---

# Notes

### Flow

```

Client
↓
Wallet Service
↓
Circuit Breaker
↓
Identity Service

```

### Best Practice

One dependency = One Circuit Breaker

Example:

- Identity Service → identityCircuitBreaker
- Fraud Service → fraudCircuitBreaker
- Notification Service → notificationCircuitBreaker

---

# Lesson 3 – Retry Pattern (Theory + Complete Implementation)

## Goal

Retry **transient failures** safely using **Resilience4j Retry** while avoiding retry storms and excessive load.

---

## What is Retry?

Retry automatically attempts the same operation again when a **temporary failure** occurs.

Examples of temporary failures:

- Temporary network issue
- Socket timeout
- Service restarting
- Temporary database connection issue

Instead of immediately failing, the application retries a limited number of times.

---

## Step 1 – Dependency

Already added in Lesson 2.

---

## Step 2 – Configuration

```yaml
resilience4j:
  retry:
    instances:
      identityRetry:
        maxAttempts: 3
        waitDuration: 2s
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - java.net.SocketTimeoutException
          - java.io.IOException
```

---

## Configuration Explanation

| Property | Meaning |
|-----------|----------|
| maxAttempts | Maximum retry attempts |
| waitDuration | Wait time before retry |
| enableExponentialBackoff | Increase wait time after every retry |
| exponentialBackoffMultiplier | Multiplies delay each retry |
| retryExceptions | Exceptions eligible for retry |

---

## Step 3 – Service Implementation

```java
@CircuitBreaker(
        name = "identityService",
        fallbackMethod = "identityFallback"
)
@Retry(
        name = "identityRetry",
        fallbackMethod = "retryFallback"
)
public UserSummaryResponse getUser(UUID userId) {

    return identityFeignClient.getUser(userId);

}
```

---

## Retry Fallback

```java
private UserSummaryResponse retryFallback(
        UUID userId,
        Throwable exception
) {

    return new UserSummaryResponse(
            userId,
            "Unknown",
            "User",
            null,
            "SERVICE Retry Failed"
    );

}
```

---

## Circuit Breaker Fallback

```java
private UserSummaryResponse identityFallback(
        UUID userId,
        Throwable exception
) {

    return new UserSummaryResponse(
            userId,
            "Unknown",
            "User",
            "unavailable@example.com",
            "SERVICE_UNAVAILABLE"
    );

}
```

---

## Important Note

```java
name = "identityRetry"
```

comes from

```yaml
resilience4j:
  retry:
    instances:
      identityRetry:
```

---

## What Happens if the Server is Down?

```
Retry

↓

Retry

↓

Retry

↓

Retry Fallback

↓

Circuit Breaker observes failure

↓

Eventually Circuit Breaker opens
```

---

## Retry + Circuit Breaker (Correct Order)

```
Retry

↓

Circuit Breaker

↓

Identity Service
```

The Circuit Breaker observes the **final outcome**.

If failures continue, the Circuit Breaker eventually opens.

---

# When NOT to Retry

Never retry:

- Validation errors
- Bad requests (400)
- Authentication failures
- Authorization failures
- Duplicate business operations

---

# Common Mistakes

### ❌ Retrying every exception

Retry only transient failures.

---

### ❌ No Backoff

Immediate retries increase pressure on failing systems.

---

### ❌ High Retry Count

More retries

≠

Better reliability.

---

### ❌ Retrying HTTP 400

Client must fix the request.

---

### ❌ Retrying Non-idempotent Operations

Risk of duplicate transactions.

---

# Interview Questions

## 1. What problem does Retry solve?

### Answer

It improves resilience against transient failures by automatically retrying operations that may succeed on a subsequent attempt.

---

## 2. What is a retry storm?

### Answer

Large numbers of retries from many clients overload an already struggling service, making recovery more difficult.

---

## 3. Why use exponential backoff?

### Answer

To gradually increase the delay between retries, reducing pressure on downstream services.

---

## 4. Should authentication failures be retried?

### Answer

No.

Authentication failures are **not transient failures**.

---

## 5. What is idempotency?

### Answer

An operation is **idempotent** if executing it multiple times produces the same effect as executing it once.

---

## 6. Why are retries dangerous for payment systems?

### Answer

Without idempotency, retries can create duplicate business operations, such as charging a customer multiple times.

---

# Notes

## Retry Flow

```
Call Service

↓

Temporary Failure

↓

Retry

↓

Success
```

or

```
Call Service

↓

Retry

↓

Retry

↓

Retry

↓

Fallback
```

---

# Lesson 4 – Time Limiter (Theory + Complete Implementation)

## Goal

Stop waiting indefinitely for slow services and protect application resources using **Resilience4j TimeLimiter**.

---

## Why TimeLimiter?

Never wait forever for a slow service.

Waiting indefinitely:

- Blocks threads
- Occupies server resources
- Reduces throughput
- Makes users wait unnecessarily

TimeLimiter stops waiting after a configured duration.

---

## Step 1 – YML Configuration

```yaml
resilience4j:
  timelimiter:
    instances:
      identityTimeout:
        timeoutDuration: 3s
        cancelRunningFuture: true
```

---

## Configuration Explanation

| Property | Meaning |
|-----------|----------|
| timeoutDuration | Maximum waiting time |
| cancelRunningFuture | Attempts to cancel the running async task |

---

## Step 2 – Service Layer

```java
@Service
@RequiredArgsConstructor
public class WalletServiceImpl {

    private final IdentityFeignClient identityFeignClient;

    @TimeLimiter(
            name = "identityTimeout",
            fallbackMethod = "timeoutFallback"
    )
    public CompletableFuture<UserSummaryResponse> getUser(UUID userId) {

        return CompletableFuture.supplyAsync(
                () -> identityFeignClient.getUser(userId)
        );

    }

    private CompletableFuture<UserSummaryResponse> timeoutFallback(
            UUID userId,
            Throwable exception
    ) {

        return CompletableFuture.completedFuture(

                new UserSummaryResponse(
                        userId,
                        "Timed Out",
                        false
                )

        );

    }

}
```

---

## Why CompletableFuture?

TimeLimiter works with **asynchronous execution**.

```
CompletableFuture = Asynchronous Programming
```

Without asynchronous execution, TimeLimiter cannot stop waiting properly.

---

# Best Practice Order

```
Retry

↓

TimeLimiter

↓

Circuit Breaker

↓

Service
```

---

# Common Mistakes

### ❌ Huge Timeout

Waiting 60 seconds rarely improves user experience.

---

### ❌ Tiny Timeout

Legitimate requests fail unnecessarily.

---

### ❌ No Timeout

Threads wait indefinitely.

---

### ❌ Returning Raw Timeout Exceptions

Provide meaningful fallback responses.

---

### ❌ Assuming Cancellation Always Stops Work

Cancellation only requests cooperation.

Some operations may continue running in the background.

---

# Interview Questions

## 1. Why is a slow service dangerous?

### Answer

Because it keeps threads and other resources occupied, reducing the application's ability to process new requests.

---

## 2. What does TimeLimiter do?

### Answer

It limits how long the application waits for an asynchronous operation before timing out and optionally returning a fallback.

---

## 3. Why use CompletableFuture?

### Answer

TimeLimiter operates on asynchronous executions so it can stop waiting without blocking indefinitely.

---

## 4. Does TimeLimiter stop the remote service?

### Answer

No.

It only stops waiting for the response.

The remote service may continue processing the request.

---

## 5. Why combine TimeLimiter with Circuit Breaker?

### Answer

Repeated timeouts count as failures, allowing the Circuit Breaker to detect an unhealthy dependency and eventually fail fast.

---

## 6. Should every service have the same timeout?

### Answer

No.

Timeout values should be based on each dependency's expected latency and business requirements.

---

# Notes

## TimeLimiter Flow

```
Wallet Service

↓

TimeLimiter

↓

Identity Service

↓

Response within 3 seconds?

├── Yes → Return Response
│
└── No
      ↓
   Timeout
      ↓
   Fallback
```

---

# Lesson 5 – Bulkhead Pattern (Theory + Complete Implementation)

## Goal

Isolate resources so one slow or overloaded dependency cannot exhaust all application threads or connections.

---

# Basic Idea

Imagine your Wallet Service has **200 threads**.

Without Bulkhead:

- Identity Service uses all 200 threads.
- Fraud Service cannot execute.
- Notification Service also waits.
- Entire Wallet Service becomes slow.

Instead, divide the resources.

Example:

- Identity Service → 20 threads
- Fraud Service → 10 threads
- Notification Service → 10 threads
- Wallet Business Logic → 160 threads

Even if Identity becomes slow, the remaining threads continue serving other requests.

> **Remember:** This happens **inside the same service**. We are simply dividing resources to protect them.

---

# Types of Bulkhead

## 1. Semaphore Bulkhead

Limits concurrent execution.

Example:

Maximum concurrent requests = **20**

- Request 1–20 → Allowed
- Request 21 → Rejected

---

## 2. Thread Pool Bulkhead

Instead of sharing one thread pool, each dependency gets its own dedicated thread pool.

Example:

- Identity Pool → 20 threads
- Fraud Pool → 10 threads
- Notification Pool → 10 threads

Again, this is **inside the same service**.

---

# Semaphore vs Thread Pool Bulkhead

| Semaphore Bulkhead | Thread Pool Bulkhead |
|--------------------|----------------------|
| Synchronous work | Asynchronous work |
| Very lightweight | Better isolation |
| Low overhead | Separate executor |
| Most common | Used for long-running tasks |

---

# How to Use

## Step 1 – Semaphore Configuration

```yaml
resilience4j:
  bulkhead:
    instances:
      identityBulkhead:
        maxConcurrentCalls: 20
        maxWaitDuration: 500ms
```

### Configuration Explanation

| Property | Meaning |
|----------|---------|
| maxConcurrentCalls | Maximum concurrent requests |
| maxWaitDuration | Maximum waiting time before rejection |

---

## Thread Pool Bulkhead Configuration

```yaml
resilience4j:
  thread-pool-bulkhead:
    instances:
      identityBulkhead:
        coreThreadPoolSize: 5
        maxThreadPoolSize: 10
        queueCapacity: 20
```

---

## Step 2 – Service Layer

```java
@Bulkhead(
        name = "identityBulkhead",
        type = Bulkhead.Type.SEMAPHORE,
        fallbackMethod = "bulkheadFallback"
)

public UserSummaryResponse getUser(UUID userId) {

    return identityFeignClient.getUser(userId);

}
```

---

## Bulkhead Fallback

```java
private UserSummaryResponse bulkheadFallback(
        UUID userId,
        Throwable exception
) {

    return new UserSummaryResponse(
            userId,
            "System Busy",
            false
    );

}
```

---

# Internal Flow

```
Incoming Request

↓

Bulkhead

↓

Concurrent Slots Available?

├── Yes
│      ↓
│ Identity Service
│
└── No
       ↓
Fallback / Reject Request
```

---

# Common Mistakes

### ❌ One Huge Bulkhead

No isolation.

---

### ❌ Too Few Concurrent Calls

Legitimate traffic gets rejected.

---

### ❌ Too Many Concurrent Calls

Bulkhead loses effectiveness.

---

### ❌ Sharing One Bulkhead Across Dependencies

Each downstream dependency should have its own Bulkhead.

---

### ❌ Returning Generic HTTP 500

Return a meaningful fallback such as:

```
System Busy
```

---

# Interview Questions

## 1. What problem does Bulkhead solve?

### Answer

It isolates resources so one overloaded or slow dependency cannot consume all available threads or concurrent execution capacity.

---

## 2. Why is it called Bulkhead?

### Answer

It comes from ships.

Watertight compartments prevent flooding in one area from sinking the entire ship.

---

## 3. Difference between Semaphore and Thread Pool Bulkhead?

### Answer

Semaphore Bulkhead

- Limits concurrent executions.

Thread Pool Bulkhead

- Uses dedicated thread pools to isolate work.

---

## 4. Why reject requests instead of waiting?

### Answer

Waiting consumes resources.

Fast rejection protects the application's responsiveness.

---

## 5. Should every dependency share one Bulkhead?

### Answer

No.

Different downstream services should usually have separate Bulkheads.

---

## 6. Is Bulkhead the same as Circuit Breaker?

### Answer

No.

Bulkhead

- Limits resource usage.

Circuit Breaker

- Stops calling unhealthy services.

---

# Notes

## Bulkhead Flow

```
Wallet Service

↓

Bulkhead

↓

Identity Service
```

One dependency becoming slow should **never** consume every thread in the application.

---

---

# Lesson 6 – Rate Limiter (Theory + Complete Implementation)

## Goal

Protect your microservices from excessive traffic by limiting how many requests are processed within a given time.

---

# Basic Idea

Imagine your service can process:

```
500 requests / second
```

Suddenly,

```
50,000 requests / second
```

arrive because of:

- Bot traffic
- Malicious attack
- Buggy client
- Traffic spike

Without Rate Limiter:

- CPU spikes
- Memory increases
- Database overload
- Poor customer experience

Rate Limiter protects the service by rejecting excess requests.

---

# Rate Limiter vs Bulkhead

| Rate Limiter | Bulkhead |
|---------------|----------|
| Controls request rate | Controls concurrent execution |
| Protects against traffic spikes | Protects resources |
| Based on time | Based on capacity |
| Rejects excess requests | Rejects excess concurrent work |

---

# How to Use

## Step 1 – Configuration

```yaml
resilience4j:
  ratelimiter:
    instances:
      walletRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0
```

---

## Configuration Explanation

| Property | Meaning |
|----------|---------|
| limitForPeriod | Maximum requests allowed |
| limitRefreshPeriod | Permit reset interval |
| timeoutDuration | Waiting time for permit (0 = reject immediately) |

---

## Step 2 – Service Implementation

```java
@RateLimiter(
        name = "walletRateLimiter",
        fallbackMethod = "rateLimitFallback"
)

public UserSummaryResponse getUser(UUID userId) {

    return identityFeignClient.getUser(userId);

}
```

---

# Internal Flow

```
Incoming Request

↓

Rate Limiter

↓

Permit Available?

├── Yes
│      ↓
│ Identity Service
│      ↓
│ Response
│
└── No
       ↓
Reject Request
```

---

# Per User vs Global Rate Limit

## Per User

Each user has their own limit.

Example:

```
User A → 100 req/min

User B → 100 req/min
```

---

## Global

All users share one limit.

Example:

```
Entire Application

↓

100 requests/minute
```

---

Most production APIs use:

- Per User
- Per Client
- Per API Key

instead of a single global limit.

---

# Where Should Rate Limiting Be Applied?

Best location:

```
Client

↓

API Gateway

↓

Rate Limiter

↓

Wallet Service
```

We don't want unwanted traffic entering the service.

---

# HTTP Status Code

When a request exceeds the configured rate limit, return:

```
HTTP 429

Too Many Requests
```

The server is healthy.

It is intentionally refusing excess traffic.

---

# Interview Questions

## 1. What problem does Rate Limiter solve?

### Answer

It limits the number of requests processed during a given period, protecting services from overload and abuse.

---

## 2. Difference between Rate Limiter and Bulkhead?

### Answer

Rate Limiter

- Controls request rate over time.

Bulkhead

- Controls concurrent execution capacity.

---

## 3. Why return HTTP 429?

### Answer

Because the server is healthy but intentionally refuses excess requests to protect itself.

---

## 4. Where is the best place for Rate Limiting?

### Answer

Usually at the API Gateway because it blocks unnecessary traffic before reaching downstream services.

---

## 5. Why are per-user limits preferable?

### Answer

They prevent one client from consuming the allowance intended for everyone else.

---

## 6. Should every endpoint have the same rate limit?

### Answer

No.

Different endpoints have different business importance and processing costs.

Rate limits should be tuned accordingly.

---

# Notes

## Rate Limiter Flow

```
Client

↓

API Gateway

↓

Rate Limiter

↓

Wallet Service

↓

Identity Service
```

---

## Summary

| Pattern | Protects Against |
|----------|------------------|
| Retry | Temporary failures |
| TimeLimiter | Slow services |
| Bulkhead | Resource exhaustion |
| Circuit Breaker | Repeated failures |
| Rate Limiter | Traffic spikes |

---

# Lesson 7 – Combining Resilience Patterns (Production Implementation)

## Goal

Build a **production-ready resilience pipeline** for the Wallet Service when calling the Identity Service.

---

# Each Pattern and Its Responsibility

| Pattern | Protects Against |
|----------|------------------|
| Rate Limiter | Too much incoming traffic |
| Retry | Temporary failures |
| TimeLimiter | Slow responses |
| Bulkhead | Resource exhaustion |
| Circuit Breaker | Repeated failures |

---

# Overall Architecture

```text
                Client
                   │
                   ▼
             API Gateway
                   │
                   ▼
            Rate Limiter
                   │
                   ▼
            Wallet Service
                   │
                   ▼
      Retry (Transient failures)
                   │
                   ▼
      TimeLimiter (Slow responses)
                   │
                   ▼
      Bulkhead (Resource isolation)
                   │
                   ▼
Circuit Breaker (Dependency health)
                   │
                   ▼
           Identity Service
```

Here, the **Wallet Service** calls the **Identity Service**.

---

# Why This Order?

Let's examine it one step at a time.

---

## Step 1 – Rate Limiter

```text
Incoming Requests

↓

Rate Limiter
```

### Why first?

Reject unnecessary requests as early as possible.

It is the **cheapest** operation.

Don't waste:

- CPU
- Memory
- Threads
- Network calls

---

## Step 2 – Retry

```text
Retry

↓

Identity
```

Temporary failure?

Retry.

Permanent failure?

Eventually fail.

---

## Step 3 – TimeLimiter

```text
Retry

↓

TimeLimiter

↓

Identity
```

Never wait forever.

Slow services become timeout failures.

---

## Step 4 – Bulkhead

```text
Bulkhead

↓

Identity
```

Protect Wallet Service resources.

Even if Identity Service hangs, Wallet Service still has available threads for other operations.

---

## Step 5 – Circuit Breaker

```text
Circuit Breaker

↓

Identity
```

After enough failures,

Stop calling Identity Service completely.

Fail fast.

---

# Complete Production Flow

```text
Client

↓

API Gateway

↓

Rate Limiter

↓

Retry

↓

TimeLimiter

↓

Bulkhead

↓

Circuit Breaker

↓

Identity Service

↓

Response
```

---

# Why This Pipeline Works

## Rate Limiter

Stops traffic spikes before they reach your service.

---

## Retry

Recovers temporary failures automatically.

---

## TimeLimiter

Prevents requests from waiting forever.

---

## Bulkhead

Protects application resources from being exhausted.

---

## Circuit Breaker

Stops repeatedly calling unhealthy services.

---

# Summary

| Step | Purpose |
|------|---------|
| Rate Limiter | Reject unnecessary traffic |
| Retry | Recover transient failures |
| TimeLimiter | Prevent long waits |
| Bulkhead | Protect resources |
| Circuit Breaker | Stop repeated failures |

---

# Lesson 8 – Final Project: Production-Ready Resilient Wallet Service

## Goal

Build a Wallet Service that remains **stable** and **responsive** even when downstream services:

- Fail
- Become slow
- Receive excessive traffic

---

# Final Resilience Pipeline

Every request to Identity Service follows this pipeline.

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
Retry
   │
   ▼
TimeLimiter
   │
   ▼
Bulkhead
   │
   ▼
Circuit Breaker
   │
   ▼
Identity Service
```

---

# Failure Matrix

| Failure | Pattern That Helps |
|----------|--------------------|
| Traffic spike | Rate Limiter |
| Temporary network issue | Retry |
| Slow response | TimeLimiter |
| Thread exhaustion | Bulkhead |
| Service outage | Circuit Breaker |

---

# Step 1 – Configuration

```yaml
resilience4j:

  ratelimiter:
    instances:
      walletRateLimiter:
        limitForPeriod: 100
        limitRefreshPeriod: 1s
        timeoutDuration: 0

  retry:
    instances:
      identityRetry:
        maxAttempts: 2
        waitDuration: 500ms
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2

  timelimiter:
    instances:
      identityTimeout:
        timeoutDuration: 2s
        cancelRunningFuture: true

  bulkhead:
    instances:
      identityBulkhead:
        maxConcurrentCalls: 20
        maxWaitDuration: 200ms

  circuitbreaker:
    instances:
      identityCircuitBreaker:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 15s
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
```

---

# Step 2 – Wallet Service Modification

```java
@RateLimiter(name = "walletRateLimiter")

@Retry(name = "identityRetry")

@TimeLimiter(name = "identityTimeout")

@Bulkhead(
        name = "identityBulkhead",
        type = Bulkhead.Type.THREADPOOL
)

@CircuitBreaker(
        name = "identityCircuitBreaker",
        fallbackMethod = "identityFallback"
)

public CompletableFuture<UserSummaryResponse> getUser(UUID userId) {

    return CompletableFuture.supplyAsync(() ->
            identityFeignClient.getUser(userId)
    );

}
```

---

# Fallback Method

```java
private CompletableFuture<UserSummaryResponse> identityFallback(
        UUID userId,
        Throwable throwable
) {

    return CompletableFuture.completedFuture(

            new UserSummaryResponse(

                    userId,
                    "Identity Service Unavailable",
                    "unknown",
                    "mehedihasanraj.com",
                    "unavailable"

            )

    );

}
```

---

# Complete Production Request Flow

```text
Client

↓

API Gateway

↓

Rate Limiter

↓

Retry

↓

TimeLimiter

↓

Bulkhead

↓

Circuit Breaker

↓

Identity Service

↓

Response
```

---

# Why This Is Production Ready

✅ Handles traffic spikes

↓

Rate Limiter

---

✅ Handles temporary failures

↓

Retry

---

✅ Prevents infinite waiting

↓

TimeLimiter

---

✅ Protects application resources

↓

Bulkhead

---

✅ Prevents repeatedly calling dead services

↓

Circuit Breaker

---

# Complete Failure Handling

```text
Incoming Request

↓

Rate Limiter
│
├── Too many requests?
│
└── Reject (HTTP 429)

↓

Retry
│
├── Temporary failure?
│
└── Retry

↓

TimeLimiter
│
├── Response within timeout?
│
└── Timeout

↓

Bulkhead
│
├── Thread available?
│
└── Reject

↓

Circuit Breaker
│
├── Healthy?
│
└── Open Circuit

↓

Identity Service

↓

Fallback (if necessary)

↓

Client Response
```

---

# Best Practices

- One Circuit Breaker per downstream dependency.
- One Bulkhead per downstream dependency.
- Retry **only** transient failures.
- Always configure reasonable timeouts.
- Use exponential backoff with Retry.
- Apply Rate Limiting at the API Gateway whenever possible.
- Return meaningful fallback responses instead of raw exceptions.
- Monitor resilience metrics in production.
- Tune thresholds based on real production traffic.

---

# Common Mistakes

### ❌ Retrying every exception

Retry only transient failures.

---

### ❌ Using one Circuit Breaker for everything

Each dependency should have its own Circuit Breaker.

---

### ❌ No timeout

Threads wait indefinitely.

---

### ❌ Huge Bulkhead

No real isolation.

---

### ❌ Same rate limit for every endpoint

Different endpoints have different business importance.

---

### ❌ Returning generic HTTP 500

Provide meaningful fallback responses whenever appropriate.

---

# Final Revision Cheat Sheet

| Pattern | Main Purpose | Protects Against |
|----------|--------------|------------------|
| **Rate Limiter** | Limit incoming requests | Traffic spikes |
| **Retry** | Retry temporary failures | Transient errors |
| **TimeLimiter** | Stop waiting forever | Slow services |
| **Bulkhead** | Isolate resources | Thread exhaustion |
| **Circuit Breaker** | Stop unhealthy calls | Service outages |

---

# Golden Order to Remember ⭐

```text
Client
    ↓
API Gateway
    ↓
Rate Limiter
    ↓
Retry
    ↓
TimeLimiter
    ↓
Bulkhead
    ↓
Circuit Breaker
    ↓
Identity Service
```

> **Easy memory trick:**  
> **Control → Retry → Timeout → Isolate → Stop**
>
> - **Control** → Rate Limiter
> - **Retry** → Retry
> - **Timeout** → TimeLimiter
> - **Isolate** → Bulkhead
> - **Stop** → Circuit Breaker

---

# Repository 07 Completed ✅

You now have complete notes covering:

- Why Microservices Fail
- Circuit Breaker
- Retry
- TimeLimiter
- Bulkhead
- Rate Limiter
- Combining Resilience Patterns
- Production-Ready Wallet Service
- Best Practices
- Common Mistakes
- Interview Questions
- Complete Request Flow
- Final Revision Cheat Sheet