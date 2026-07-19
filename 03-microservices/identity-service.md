# User Identity Service

## Common Mistakes

❌ **Storing raw passwords**
- Never store plain text passwords.
- Always store **hashed passwords**.

❌ **Using email as the primary key**
- Email addresses can change.
- Primary keys should be immutable.

❌ **No unique index on email**
- Always create a unique constraint on the email column.

❌ **Exposing database IDs**
- Never expose internal database IDs.
- Use a **public UUID** instead.

### Why two IDs?

We use two types of IDs:

- **Long (Primary Key)** → Internal database identifier.
- **UUID** → Public identifier exposed through APIs.

Example:

```text
Database
---------
id (Long) = 1
publicId (UUID) = 4f8dce0a-84d0-47f4-b7f5-a1d9f1d4ec11
```

Why?

- Long IDs are fast for indexing and joins.
- UUIDs prevent users from guessing sequential IDs.

---

# Interview Questions

## 1. Why use UUID as the public identifier?

UUIDs:

- Difficult to guess
- Don't reveal database size
- Remain stable even if persistence strategy changes

---

## 2. Why use a separate database ID?

Numeric IDs are:

- Faster indexes
- Better foreign keys
- Smaller storage
- Better database performance

UUID is only exposed externally.

---

## 3. Why shouldn't email be the primary key?

Emails can change.

Primary keys should never change.

---

## 4. Why store password hashes instead of passwords?

Hashes are **one-way**.

Even if the database is compromised, attackers cannot directly recover user passwords.

---

## 5. Why use optimistic locking (`@Version`)?

To detect concurrent updates and prevent one user's changes from silently overwriting another's.

---

# Business Layer

## Step 1 - Add Spring Security Crypto

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

---

## Step 2 - Password Configuration

Create a configuration class.

Expose a `PasswordEncoder` bean.

```java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

---

## Step 3 - User Service

Use `@Transactional` on registration.

```java
@Transactional
public User register(RegisterRequest request){
    ...
}
```

### Why?

Registration includes:

- Validation
- Entity creation
- Password hashing
- Database save

If anything fails, the entire transaction rolls back.

---

## Common Mistakes

❌ Using `equalsIgnoreCase()` everywhere.

Better approach:

Normalize once.

```java
email = email.toLowerCase(Locale.ROOT);
```

Store normalized values.

---

❌ Creating `new BCryptPasswordEncoder()` directly.

Better:

Inject `PasswordEncoder`.

---

# API Layer

| Method | Endpoint | Purpose |
|---------|----------|----------|
| POST | `/api/v1/users` | Register user |
| GET | `/api/v1/users/{userId}` | Get user |
| GET | `/api/v1/users/email/{email}` *(Internal)* | Lookup by email |

---

# Docker & MySQL

Create a Docker network.

```bash
docker network create identity-service
```

Run MySQL.

```bash
docker run -d \
  --name mysql-db-2 \
  --network identity-service \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=identitydb \
  -e MYSQL_USER=appuser \
  -e MYSQL_PASSWORD=apppassword \
  -p 3307:3306 \
  --restart unless-stopped \
  mysql:8.4
```

Why port **3307**?

Wallet Service is already using **3306**.

---

# REST Communication Between Microservices

Inside Wallet Service create packages:

```
client
dto
exception
```

These packages are responsible for calling the Identity Service.

---

## Common Structure

```
Wallet Service
│
├── client
├── dto
├── exception
└── config
```

---

# Communication Steps

## Step 1

Add service URL.

```yaml
identity-service:
  base-url: http://localhost:8081
```

Later replace with service discovery.

---

## Step 2

Create a `RestClient` bean.

```java
@Bean
RestClient restClient(RestClient.Builder builder){
    return builder.build();
}
```

---

## Step 3

Create DTO

Example:

```java
UserSummaryResponse
```

Stores the response returned from Identity Service.

---

## Step 4

Create Remote Exceptions

Example:

```
IdentityServiceException

UserNotFoundRemoteException
```

Convert HTTP errors into meaningful business exceptions.

---

## Step 5

Create Identity Client

Responsible only for communicating with Identity Service.

Business layer should not contain HTTP code.

---

# Interview Questions

## 1. Why create an IdentityClient?

To isolate HTTP communication from business logic.

If communication changes later (Feign, gRPC, Kafka), only the client changes.

---

## 2. Why doesn't Wallet use the User Entity?

Each microservice owns its own domain model.

Services communicate through **DTOs**, not entities.

---

## 3. Why not hardcode localhost?

Production URLs change.

Configuration makes code environment-independent.

---

## 4. Why catch RestClient exceptions?

Convert infrastructure errors into meaningful business exceptions.

Business logic shouldn't understand HTTP internals.

---

## 5. Why is RestClient better than RestTemplate?

RestClient is Spring's modern synchronous HTTP client with a fluent API.

It replaces RestTemplate for new development.

---

# Project Flow

```
Create Account
       │
       ▼
Identity Service
       │
User Created
       │
       ▼
Wallet Creation Request
       │
       ▼
Identity Service
Checks User Exists?
       │
   Yes ▼
Wallet Created
```

---

# Lesson 8 - Resilient REST Communication

| HTTP Status | Retry? | Reason |
|-------------|--------|--------|
| 400 | ❌ | Client bug |
| 401 | ❌ | Authentication issue |
| 403 | ❌ | Authorization issue |
| 404 | ❌ | Resource doesn't exist |
| 408 | ✅ | Request timeout |
| 429 | ✅ | Rate limiting (retry after delay) |
| 500 | ✅ | Temporary server issue |
| 503 | ✅ | Service unavailable |

---

# Lesson 9 - OpenFeign

OpenFeign removes HTTP boilerplate.

Instead of writing HTTP code manually, simply declare an interface.

## Example

```java
@FeignClient(name = "identity-service")
public interface IdentityClient {

    @GetMapping("/users/{id}")
    UserResponse getUser(UUID id);
}
```

That's it.

No HTTP code.

No RestClient.

---

## Internally

```
IdentityClient
      │
      ▼
Dynamic Proxy
      │
      ▼
HTTP Request
      │
      ▼
JSON
      │
      ▼
Identity Service
```

---

# How to Use OpenFeign

## Step 1 - Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

---

## Step 2 - Enable Feign

```java
@EnableFeignClients
@SpringBootApplication
public class WalletApplication {
}
```

---

## Step 3 - Create Feign Client

```java
@FeignClient(
    name = "identity-service",
    url = "${identity-service.base-url}"
)
public interface IdentityFeignClient {

    @GetMapping("/api/v1/users/{userId}")
    UserSummaryResponse getUser(
        @PathVariable UUID userId
    );
}
```

---

## Step 4 - Inject and Use

```java
@Service
@RequiredArgsConstructor
public class WalletService {

    private final IdentityFeignClient identityClient;

    public void createWallet(CreateWalletRequest request) {

        UserSummaryResponse user =
                identityClient.getUser(request.userId());

        // Business logic
    }
}
```

---

# How Feign Works Internally

```
IdentityFeignClient
        │
        ▼
Java Dynamic Proxy
        │
        ▼
Invocation Handler
        │
        ▼
Build HTTP Request
        │
        ▼
Jackson
        │
        ▼
JSON
        │
        ▼
HTTP
        │
        ▼
Identity Service
        │
        ▼
Response
        │
        ▼
Jackson
        │
        ▼
DTO
```

---

# Interview Questions

## Why use OpenFeign?

OpenFeign removes HTTP boilerplate using declarative interfaces while integrating cleanly with Spring Cloud features such as:

- Service Discovery
- Load Balancing
- Circuit Breakers (with Resilience4j)
- Configuration

---

## Does Feign create the implementation?

Yes.

Feign generates the implementation at runtime.

---

## Which Design Pattern does Feign use?

Feign uses the **Proxy Design Pattern**, specifically **Java Dynamic Proxy**.

The proxy intercepts method calls, converts them into HTTP requests, sends the request, and maps the response back to a DTO.

---

# Key Takeaways

- Never store raw passwords.
- Hash passwords using `BCryptPasswordEncoder`.
- Use `PasswordEncoder` injection instead of direct instantiation.
- Keep `Long` IDs internal and expose `UUID` publicly.
- Normalize emails before storing.
- Use `@Transactional` for registration.
- Each microservice owns its own entities.
- Communicate using DTOs, not entities.
- Wrap HTTP communication inside a dedicated client.
- Handle remote exceptions gracefully.
- Retry only transient HTTP failures (408, 429, 500, 503).
- Prefer `RestClient` over `RestTemplate` for new projects.
- Prefer OpenFeign to eliminate HTTP boilerplate.
- Feign implementations are generated at runtime using Java Dynamic Proxy.