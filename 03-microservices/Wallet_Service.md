# Lesson 1 - Distributed Digital Wallet

## Monolith vs Microservices

| Feature | Monolith | Microservices |
|---------|-----------|----------------|
| Deployment | One application | Many services |
| Database | One database | One database per service |
| Scaling | Scale entire application | Scale individual services |
| Performance | Faster (local calls) | Slower (network calls) |
| Complexity | Low | High |
| Team Size | Small | Large |
| Transactions | Simple | Complex |
| Cost | Lower | Higher |

---

# Service Boundaries

## User Service
Responsible for user identity and account management.

- Register User
- Update Profile
- KYC
- Authentication
- Account Status

---

## Wallet Service
Responsible for wallet management.

- Create Wallet
- Balance
- Currency
- Freeze Wallet
- Deposit
- Withdraw

---

## Transaction Service
Responsible for money movement.

- Transfer Money
- Transaction History
- Transaction Status
- Settlement

---

## Notification Service

- Email
- SMS
- Push Notification

---

## Fraud Service

- Risk Score
- Velocity Checks
- AML
- Suspicious Transfers

---

# Wallet Service

## Flyway Migration

Create the migration manually.

```
src/main/resources/db/migration
```

Create the file:

```
V1__Create_wallet_table.sql
```

SQL:

```sql
CREATE TABLE wallets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id CHAR(36),
    balance DECIMAL(19,2),
    currency VARCHAR(10),
    status VARCHAR(20),
    version BIGINT
);
```

---

# Entity Defaults using @PrePersist

```java
@PrePersist
public void prePersist() {

    if (walletId == null) {
        walletId = UUID.randomUUID();
    }

    if (balance == null) {
        balance = BigDecimal.ZERO;
    }

    if (status == null) {
        status = WalletStatus.ACTIVE;
    }
}
```

Example:

```java
Wallet wallet = new Wallet();
walletRepository.save(wallet);
```

Without `@PrePersist`, Hibernate would insert `NULL` values.

Hibernate execution flow:

```
Create Object
      ↓
@PrePersist()
      ↓
INSERT INTO wallets...
```

---

# DTOs

Return **DTOs** instead of Entities.

Benefits:

- Decouple API from persistence model
- Improve security
- Control response format
- Hide internal implementation

---

# Package by Feature

Instead of organizing by layer:

```
controller
service
repository
entity
```

Organize by business capability:

```
wallet
 ├── controller
 ├── service
 ├── repository
 ├── entity
 ├── dto
 └── mapper
```

Benefits:

- Easier navigation
- Better maintainability
- Scales well for large projects

---

# Business Layer Best Practices

## Read-only Transactions

Apply at class level:

```java
@Transactional(readOnly = true)
@Service
public class WalletServiceImpl {
}
```

Override write operations:

```java
@Transactional
public WalletResponse deposit(...) {
}
```

### Benefits

- Better performance
- Clear separation of queries and commands
- Hibernate optimization
- Prevent accidental updates

---

# BigDecimal Comparison

Don't do this:

```java
balance > amount
```

Instead:

```java
balance.compareTo(amount) < 0
```

Reason:

`BigDecimal` is an object, not a primitive.

---

# Swagger / OpenAPI

Dependency:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.9</version>
</dependency>
```

Configuration:

```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

---

# Unit Testing

Example:

```java
@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletMapper walletMapper;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficient() {

        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("100"));
        wallet.setStatus(WalletStatus.ACTIVE);

        when(walletRepository.findByWalletId(any()))
                .thenReturn(Optional.of(wallet));

        WithdrawRequest request =
                new WithdrawRequest(new BigDecimal("200"));

        assertThrows(
                InsufficientBalanceException.class,
                () -> walletService.withdraw(
                        UUID.randomUUID().toString(),
                        request
                )
        );
    }
}
```

---

# Interview Questions

## Why doesn't Wallet Service store user information?

Because user management belongs to the **Identity Service**.

Wallet Service only stores the `userId` reference.

---

## Why return DTOs instead of Entities?

- Decouples API from persistence
- Improves security
- Controls response structure
- Prevents exposing internal fields

---

## Why use Package-by-Feature?

Related classes stay together, making the project easier to understand and maintain.

---

## Why isn't Transaction inside Wallet Service?

Transaction history is a separate business capability.

It belongs to the **Transaction Service**.

---

## Why use `@Version`?

To enable **Optimistic Locking** and prevent lost updates during concurrent requests.

---

## Why validate requests in the Controller?

The Controller is the API boundary.

Invalid requests should be rejected before reaching the business layer.

---

## Why use Java Records for DTOs?

Records are:

- Immutable
- Concise
- Thread-safe
- Automatically generate:
    - constructor
    - getters
    - equals()
    - hashCode()
    - toString()

---

## Why create a Service Interface?

- Loose coupling
- Easier testing
- Multiple implementations
- Follows SOLID principles

---

## Why use `@Transactional`?

Ensures database operations succeed or fail as a single atomic unit.

---

## Why use `@Transactional(readOnly = true)`?

- Better performance
- Database optimizations
- Prevent accidental writes

---

## Why use `BigDecimal.compareTo()`?

Relational operators (`>`, `<`) do not work with objects.

`compareTo()` correctly compares numeric values regardless of scale.

---

# Production Notes

## MySQL with Docker

Run MySQL in Docker.

Create a dedicated Docker network:

```bash
docker network create wallet-network
```

Run MySQL:

```bash
docker run -d \
  --name mysql-db \
  --network wallet-network \
  ...
```

Run Wallet Service:

```bash
docker run -d \
  --name wallet-service \
  --network wallet-network \
  ...
```

---

## Database Connection

Do **not** use:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/walletdb
```

Inside Docker, `localhost` refers to the container itself.

Instead, use the MySQL container name:

```properties
spring.datasource.url=jdbc:mysql://mysql-db:3306/walletdb
```

Docker automatically resolves the container name through its internal DNS.

---

# Summary

Topics covered in Lesson 1:

- Monolith vs Microservices
- Service Boundaries
- Flyway Migration
- Entity Lifecycle (`@PrePersist`)
- DTOs
- Package-by-Feature
- Transaction Management
- BigDecimal Comparison
- Swagger/OpenAPI
- Unit Testing with Mockito
- Common Interview Questions
- Docker Networking
- Production Best Practices
