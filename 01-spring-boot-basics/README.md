# Spring Boot Notes

## 1. Spring Boot Internals

When you run a Spring Boot application:

```text
main()
    ↓
SpringApplication.run()
    ↓
Creates Application Context
    ↓
Scans Packages
    ↓
Creates Beans
    ↓
Injects Dependencies
    ↓
Starts Embedded Tomcat
    ↓
Application Ready
```

---

# Creating a Spring Boot Project

| Property | Description | Example |
|----------|-------------|---------|
| Group ID | Organization / Base package | `com.raj.wallet` |
| Artifact ID | Project name (JAR name) | `wallet-service` |
| Name | Human-readable project name | `wallet-service` |
| Package Name | Base package | `com.raj.wallet` |

---

# Understanding `pom.xml`

`pom.xml` is Maven's configuration file.

It contains:

- Project information
- Dependencies
- Plugins
- Build settings

Think of it as the **blueprint** of your project.

Application startup flow:

```text
Run Application
      ↓
Read pom.xml
      ↓
Load Dependencies
      ↓
Create Spring Context
      ↓
Scan Packages
      ↓
Create Beans
      ↓
Inject Dependencies
      ↓
Start Embedded Tomcat
      ↓
Application Ready
```

---

# Spring Core Concepts

## Application Context

The **Application Context** is Spring's IoC container.

Every Spring-managed object (Bean) lives inside this container.

---

## Bean

A **Bean** is an object created and managed by Spring.

Instead of:

```java
new WalletService();
```

Spring creates the object for you.

Beans are created using annotations such as:

- `@Component`
- `@Service`
- `@Repository`
- `@Controller`
- `@RestController`

---

## IoC (Inversion of Control)

Spring takes responsibility for creating objects.

Instead of writing:

```java
new WalletService();
```

Spring creates and manages it.

---

## Dependency Injection (DI)

Example:

```
WalletController
       ↓
WalletService
```

Spring automatically injects the required dependency.

### Types of Dependency Injection

- Constructor Injection ✅ (Recommended)
- Setter Injection
- Field Injection

### Why Constructor Injection?

- Required dependencies
- Immutable objects
- Easier testing
- Recommended by the Spring Team

`@Autowired` is generally unnecessary for constructor injection in modern Spring Boot.

---

# DispatcherServlet

`DispatcherServlet` is the **Front Controller** of Spring MVC.

Every HTTP request first arrives here.

```text
Client
   ↓
DispatcherServlet
   ↓
Controller
   ↓
Service
```

---

# 2. Controller

## `@RestController`

`@RestController` combines:

- `@Controller`
- `@ResponseBody`

It returns **JSON** or plain text instead of HTML.


---

# 3. DTO (Data Transfer Object)

## Why use DTO?

Never expose Entity objects directly.

Instead:

```text
Client
   ↓
DTO
   ↓
Service
```

Benefits:

- Hide sensitive fields
- Control API response
- Separate API from Database model

### Professional Data Flow

```text
Client
   ↓
Request DTO
   ↓
Controller
   ↓
Service
   ↓
Entity
   ↓
Database
   ↓
Entity
   ↓
Response DTO
   ↓
Controller
   ↓
JSON
   ↓
Client
```

---

# Request Mapping

## `@PathVariable`

Used for identifying a resource.

Example:

```
GET /wallets/5
```

---

## `@RequestParam`

Used for filtering/searching.

Example:

```
GET /wallets/search?owner=Raj
```

---

## `@RequestBody`

Converts JSON into a Java object.

Example:

```java
@PostMapping
public void createWallet(@RequestBody Wallet wallet)
```

---

# ResponseEntity

`ResponseEntity` gives full control over:

- HTTP Status
- Headers
- Response Body

Examples:

```java
ResponseEntity.ok(wallet);

ResponseEntity.status(HttpStatus.CREATED)
              .body(wallet);
```

---

# Common HTTP Status Codes

| Code | Meaning |
|------|----------|
| 200 | OK |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request |
| 401 | Unauthorized |
| 404 | Not Found |
| 409 | Conflict |
| 500 | Internal Server Error |

---

# REST API Naming

Prefer RESTful URLs.

❌ Avoid

```
GET /getWallet
POST /createWallet
```

✅ Prefer

```
GET /wallets/{id}
POST /wallets
PUT /wallets/{id}
DELETE /wallets/{id}
```

---

# Bean Validation

Never trust client input.

Validation happens at the API boundary before business logic executes.

Dependency:

```
spring-boot-starter-validation
```

Example:

```java
@PostMapping
public void createWallet(@Valid @RequestBody WalletRequest request)
```

Common validation annotations:

- `@NotNull`
- `@NotBlank`
- `@Size`
- `@Positive`
- `@PositiveOrZero`
- `@Email`
- `@Pattern`

Validation belongs in the **DTO**, not inside business logic.

---

# 4. Global Exception Handling

Purpose:

Return consistent and meaningful error responses.

Flow:

```text
Client
   ↓
Controller
   ↓
Service
   ↓
Exception Thrown
   ↓
Global Exception Handler
   ↓
JSON Error Response
```

The Controller should **not** catch exceptions.

Spring forwards them to the Global Exception Handler.

---

# Custom Exception Flow

## Step 1 — Create Custom Exception


```java
public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(String message) {
        super(message);
    }

}
```

---

## Step 2 — Throw the Exception

Example:

```java
if (id <= 0) {
    throw new WalletNotFoundException(
        "Wallet with id " + id + " not found"
    );
}
```

---

## Step 3 — Create ErrorResponse DTO

Store information like:

- Timestamp
- Status
- Error
- Message
- Request Path

---

## Step 4 — Create Global Exception Handler

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(
            WalletNotFoundException ex,
            HttpServletRequest request) {

        // Build ErrorResponse

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

}
```

---

# Validation Exception Handling

Spring provides:

```java
MethodArgumentNotValidException
```

It is thrown whenever `@Valid` validation fails.

Example:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationException(...) {

    // Return 400 Bad Request

}
```

---



## Exception Handling

```
Throw Exception
       ↓
@ControllerAdvice
       ↓
ErrorResponse
       ↓
JSON Response
```

Spring automatically propagates unchecked exceptions. Most business exceptions in Spring applications extend RuntimeException.



## Global Exception Handling (Easy to Remember)

We use **3 classes**:

### 1. Throw Exception

Suppose there is **no wallet** with the requested ID.

Instead of returning `null`, we throw an exception.

Create a custom exception:

```java
public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(String message) {
        super(message);
    }

}
```

Then throw it whenever the wallet is not found:

```java
throw new WalletNotFoundException("Wallet not found");
```

---

### 2. Catch Exception

Now we need a place to **catch** this exception.

Create a class called:

```text
GlobalExceptionHandler
```

Annotate it with:

```java
@ControllerAdvice
```

This tells Spring that this class will handle exceptions thrown by any controller.

Inside this class, create a method for each exception.

Example:

```java
@ExceptionHandler(WalletNotFoundException.class)
public ResponseEntity<ErrorResponse> handleWalletNotFound(
        WalletNotFoundException ex,
        HttpServletRequest request) {

    // Create ErrorResponse
    // Return ResponseEntity

}
```

The method takes:

- `WalletNotFoundException` → the exception that was thrown
- `HttpServletRequest` → information about the current request

---

### 3. Return Error Response

Instead of returning a plain message, we create an object that stores all error details.

Create a class:

```text
ErrorResponse
```

Typical fields include:

- Timestamp
- HTTP Status
- Error
- Message
- Request Path

Inside `handleWalletNotFound()`, create an `ErrorResponse` object, fill in the details, and return it.

```java
return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(errorResponse);
```

---

## Easy Flow to Remember

```text
No Wallet Found
        ↓
Throw WalletNotFoundException
        ↓
GlobalExceptionHandler catches it
        ↓
Create ErrorResponse
        ↓
Return JSON Response (404 Not Found)
```

### Remember

**Throw → Catch → Return**

- **Throw** → `WalletNotFoundException`
- **Catch** → `GlobalExceptionHandler (@ControllerAdvice)`
- **Return** → `ErrorResponse` inside `ResponseEntity`



# 5. Configuration Management in Spring Boot

Configuration management allows you to keep application settings **outside your Java code**, making them easier to change for different environments.

---

## Change the Base URL

You can change the application's base path using:

```properties
server.servlet.context-path=/wallet
```

Without context path:

```text
http://localhost:8080/api/v1/wallets
```

With context path:

```text
http://localhost:8080/wallet/api/v1/wallets
```

---

## Using `@Value`

Spring allows injecting a single configuration value using `@Value`.

Example:

```java
@Value("${wallet.currency}")
private String currency;
```

This works well for one or two properties, but if your application has many configuration values, using `@Value` repeatedly becomes difficult to maintain.

---

## Better Approach – `@ConfigurationProperties`

Instead of injecting one property at a time, bind all related properties into a single class.

### Configuration

```properties
wallet.currency=GBP
wallet.maximum-transfer=5000
wallet.minimum-balance=0
```

### Properties Class

```java
@Component
@ConfigurationProperties(prefix = "wallet")
public class WalletProperties {

    private String currency;
    private Double maximumTransfer;
    private Double minimumBalance;

    // Getters and Setters
}
```

Now, instead of multiple `@Value` annotations, simply inject the `WalletProperties` object wherever it is needed.

```java
@Service
public class WalletService {

    private final WalletProperties walletProperties;

    public WalletService(WalletProperties walletProperties) {
        this.walletProperties = walletProperties;
    }

}
```

---

## Enable Configuration Properties

Add the following annotation to your main application class:

```java
@ConfigurationPropertiesScan
@SpringBootApplication
public class WalletApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletApplication.class, args);
    }

}
```

---

## Benefits of `@ConfigurationProperties`

- Groups related configuration together
- Cleaner than multiple `@Value` annotations
- Easier to maintain
- Type-safe configuration binding
- Recommended approach for applications with multiple configuration values

---

## Easy Flow to Remember

```text
application.properties / application.yml
                ↓
@ConfigurationProperties
                ↓
WalletProperties Object
                ↓
Inject into Service
                ↓
Use Configuration Values
```

### Remember

- **`@Value`** → Best for one or two properties.
- **`@ConfigurationProperties`** → Best for grouping related properties.
- **`@ConfigurationPropertiesScan`** → Enables automatic scanning of configuration property classes.

# 6. Logging in Spring Boot

Logging helps us understand **what happened inside the application**. It tells the application's diagnosis story and is essential for debugging, monitoring, and troubleshooting.

Spring Boot uses:

- **SLF4J** → Logging API
- **Logback** → Default logging implementation

---

## Why Logging?

Imagine a money transfer request:

```text
Transfer Request
        ↓
Validation
        ↓
Business Logic
        ↓
Database
        ↓
Response
```

Without logs, it's difficult to know where something went wrong.

Good logs tell the complete story:

```text
Transfer Request Received
        ↓
Transfer Validated
        ↓
Balance Updated
        ↓
Transaction Completed
```

---

# Creating a Logger

Create one logger for each class.

```java
private static final Logger logger =
        LoggerFactory.getLogger(WalletService.class);
```

### Why `static final`?

- **static** → One logger shared by the entire class.
- **final** → Logger reference cannot change.
- Only **one logger instance** is needed per class.

---

# Writing Logs

Example:

```java
logger.info("Fetching wallet with id {}", id);
```

Using `{}` is preferred over string concatenation because it is more efficient.

---

# Log Levels

```text
TRACE
DEBUG
INFO
WARN
ERROR
```

### When to use each level

| Level | Use Case |
|--------|----------|
| TRACE | Very detailed execution flow |
| DEBUG | Variable values and debugging |
| INFO | Normal application events |
| WARN | Unexpected but recoverable issues |
| ERROR | Serious failures |

Examples:

```text
TRACE → Entering transfer method

DEBUG → Wallet balance = 5000

INFO → Wallet created successfully

WARN → Transfer amount exceeds recommended limit

ERROR → Database connection failed
```

### Environment Usage

Development:

- TRACE
- DEBUG

Production:

- INFO
- WARN
- ERROR

Choose the correct level based on the importance of the message.

---

# Logging Exceptions

Prefer passing the exception object instead of only logging the message.

```java
logger.error("Wallet {} not found", id, exception);
```

This logs:

- Error message
- Stack trace
- Exception details

---

# Configure Log Level

In `application.properties`

```properties
logging.level.root=INFO
```

Example:

```properties
logging.level.com.example.wallet=DEBUG
```

---

# Customize Console Output

```properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%level] %logger - %msg%n
```

Example output:

```text
2026-07-03 14:30:15 [INFO] WalletService - Wallet created successfully
```

---

# Log to a File

Common in production applications.

```properties
logging.file.name=logs/wallet-service.log
```

Logs will be stored inside:

```text
logs/
    wallet-service.log
```

---

# Summary

```text
Application
      ↓
Logger (SLF4J)
      ↓
Logback
      ↓
Console / File
```

---

# 7. Testing in Spring Boot

Testing verifies that the application behaves correctly.

Most professional projects contain **more Unit Tests** than Integration Tests.

Types of testing:

- Unit Test
- Integration Test
- End-to-End Test

---

# Unit Test

Tests **one class in isolation**.

Example:

```text
WalletService
```

No:

- Database
- HTTP Server
- Spring Context

Characteristics:

- Very fast
- Easy to write
- Most commonly used

---

# Integration Test

Tests how multiple components work together.

Example:

```text
Controller
      ↓
Service
      ↓
Repository
      ↓
Database
```

Characteristics:

- Uses Spring Context
- Slower than Unit Tests
- More realistic

---

# End-to-End Test (E2E)

Tests the application like a real user.

Example:

```text
Browser
      ↓
API Gateway
      ↓
Wallet Service
      ↓
Database
```

Characteristics:

- Slowest
- Tests the complete system

---

# Common Assertions

```java
assertEquals()
assertTrue()
assertFalse()
assertNull()
assertNotNull()
assertThrows()
```

---

# AAA Pattern (Interview Favourite)

Most unit tests follow the **AAA Pattern**.

### 1. Arrange

Create required objects.

```java
WalletService walletService = new WalletService();
```

### 2. Act

Call the method.

```java
walletService.getWallet(1L);
```

### 3. Assert

Verify the result.

```java
assertEquals(...);
```

Flow:

```text
Arrange
    ↓
Act
    ↓
Assert
```

---

# Service Test Example

```java
class WalletServiceTest {

    private final WalletService walletService =
            new WalletService();

    @Test
    void shouldReturnWallet() {

        WalletResponse response =
                walletService.getWallet(1L);

        assertEquals(1L, response.getId());
        assertEquals("Raj", response.getOwner());

    }

    @Test
    void shouldThrowWalletNotFoundException() {

        assertThrows(
                WalletNotFoundException.class,
                () -> walletService.getWallet(-1L)
        );

    }

}
```

---

# Controller Test

Testing a controller is different.

Instead of calling methods directly, Spring simulates HTTP requests.

Use:

```java
@WebMvcTest(WalletController.class)
class WalletControllerTest {

}
```

`@WebMvcTest` loads:

- Controller
- Spring MVC

It does **not** load:

- Database
- Repository
- Full Spring Context

---

# Mocking with Mockito

Controllers depend on Services.

Instead of using the real service, Spring can create a fake one.

```java
@MockBean
private WalletService walletService;
```

This replaces the real service with a mock object.

Example:

```java
when(walletService.getWallet(1L))
        .thenReturn(
                new WalletResponse(
                        1L,
                        "Raj",
                        BigDecimal.valueOf(5000)
                )
        );
```

Meaning:

> Whenever `walletService.getWallet(1L)` is called, return this object.

---

# MockMvc

Instead of opening Postman, Spring provides **MockMvc**.

Example:

```java
mockMvc.perform(
        get("/api/v1/wallets/1")
)
.andExpect(status().isOk());
```

This simulates:

```http
GET /api/v1/wallets/1
```

without starting a real server.

---

# Test Naming Convention

Use descriptive method names.

Examples:

```java
shouldReturnWallet()

shouldCreateWallet()

shouldThrowExceptionWhenWalletNotFound()
```

A test name should clearly describe the expected behavior.

---

# What Should Be Tested?

Always test:

- Business Logic
- Validation
- Exception Handling
- Edge Cases
- HTTP Status Codes

---

# Testing Flow

```text
Unit Test
      ↓
Arrange
      ↓
Act
      ↓
Assert
      ↓
Pass / Fail
```

---

# Quick Revision

## Logging

```text
SLF4J
     ↓
Logback
     ↓
Console / File
```

## Testing Types

```text
Unit Test
    ↓
One Class

Integration Test
    ↓
Multiple Components

End-to-End Test
    ↓
Entire Application
```

## AAA Pattern

```text
Arrange
    ↓
Act
    ↓
Assert
```

## Controller Testing

```text
MockMvc
      ↓
Fake HTTP Request
      ↓
Controller
      ↓
Response
```

## Mocking

```text
Real Service
      ❌

Mock Service
      ↓

Return Fake Data
```
