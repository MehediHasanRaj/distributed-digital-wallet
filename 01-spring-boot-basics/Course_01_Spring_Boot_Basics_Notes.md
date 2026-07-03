# Course 01 -- Spring Boot Basics

## Repository

`01-spring-boot-basics`

## Objective

Build a solid foundation in Spring Boot before moving to databases and
microservices.

------------------------------------------------------------------------

# Lessons Covered

## Lesson 1 -- Introduction to Spring Boot

-   What Spring Boot is
-   Spring vs Spring Boot
-   Embedded Tomcat
-   Auto Configuration
-   Production-ready applications

**Interview Focus** - What problem does Spring Boot solve? - Spring vs
Spring Boot - Embedded server

------------------------------------------------------------------------

## Lesson 2 -- Creating a Spring Boot Project

### Spring Initializr

-   Maven
-   Java
-   Spring Boot
-   Group / Artifact
-   JAR packaging

### Dependencies

-   Spring Web
-   DevTools
-   Lombok
-   Spring Boot Actuator

**Key Concepts** - `pom.xml` - Maven dependency management - Embedded
server

------------------------------------------------------------------------

## Lesson 3 -- Project Structure & Startup

### Project Structure

-   src/main/java
-   src/main/resources
-   src/test

### Startup Flow

1.  JVM starts
2.  main()
3.  SpringApplication.run()
4.  ApplicationContext
5.  Component Scan
6.  Bean Creation
7.  Dependency Injection
8.  Embedded Tomcat

------------------------------------------------------------------------

## Lesson 4 -- IoC, DI & Beans

### Concepts

-   IoC
-   Dependency Injection
-   Beans
-   ApplicationContext

### Injection Types

-   Constructor Injection ✅
-   Setter Injection
-   Field Injection (avoid)

------------------------------------------------------------------------

## Lesson 5 -- REST API Basics

### MVC Flow

Client → DispatcherServlet → Controller → Service → Response

### REST

-   @RestController
-   @GetMapping
-   Layered Architecture
-   API Versioning

------------------------------------------------------------------------

## Lesson 6 -- Professional REST APIs

### HTTP Methods

-   GET
-   POST
-   PUT
-   PATCH
-   DELETE

### Spring MVC

-   @PathVariable
-   @RequestParam
-   @RequestBody
-   ResponseEntity

### Status Codes

-   200
-   201
-   400
-   404
-   500

------------------------------------------------------------------------

## Lesson 7 -- DTOs & Layered Architecture

### DTO Types

-   Request DTO
-   Response DTO

### Layers

-   Controller
-   Service
-   Repository (later)

Never expose Entities directly.

------------------------------------------------------------------------

## Lesson 8 -- Bean Validation

### Validation

-   @Valid
-   @NotBlank
-   @NotNull
-   @Positive
-   @PositiveOrZero
-   @Size
-   @Email
-   @Pattern

Validation happens before business logic.

------------------------------------------------------------------------

## Lesson 9 -- Global Exception Handling

### Components

-   @ControllerAdvice
-   @ExceptionHandler
-   Custom Exceptions
-   ErrorResponse DTO

Use meaningful HTTP status codes and structured JSON errors.

------------------------------------------------------------------------

## Lesson 10 -- Configuration Management

### Configuration

-   application.properties
-   application.yml

### Property Binding

-   @Value
-   @ConfigurationProperties

### Profiles

-   dev
-   test
-   prod

Use environment variables for secrets.

------------------------------------------------------------------------

## Lesson 11 -- Logging

### Logging Stack

Application → SLF4J → Logback

### Levels

-   TRACE
-   DEBUG
-   INFO
-   WARN
-   ERROR

Never log passwords, tokens, or sensitive financial information.

------------------------------------------------------------------------

## Lesson 12 -- Testing

### Types

-   Unit Tests
-   Integration Tests
-   Controller Tests

### Tools

-   JUnit 5
-   Mockito
-   MockMvc
-   @WebMvcTest
-   @SpringBootTest

### AAA Pattern

-   Arrange
-   Act
-   Assert

------------------------------------------------------------------------

# Project Structure

``` text
wallet-service
│
├── controller
├── service
├── dto
│   ├── request
│   └── response
├── exception
├── config
├── WalletServiceApplication.java
└── application.properties
```

------------------------------------------------------------------------

# Best Practices

-   Prefer constructor injection.
-   Keep business logic in the Service layer.
-   Use DTOs instead of exposing entities.
-   Validate all incoming requests.
-   Handle exceptions globally.
-   Externalize configuration.
-   Use structured logging.
-   Write meaningful tests.
-   Use `BigDecimal` for money.

------------------------------------------------------------------------

# Common Interview Topics

-   Spring Boot startup lifecycle
-   IoC vs Dependency Injection
-   Bean lifecycle
-   REST API design
-   HTTP status codes
-   DTO vs Entity
-   Bean Validation
-   Global Exception Handling
-   Configuration Properties
-   Logging levels
-   Unit vs Integration testing
-   Mockito and MockMvc

------------------------------------------------------------------------

# Git History

``` bash
git commit -m "Initialize Spring Boot wallet-service project"
git commit -m "Document Spring Boot project structure and startup lifecycle"
git commit -m "Learn IoC, Dependency Injection, and Spring Beans"
git commit -m "Create first REST API with controller and service layers"
git commit -m "Build RESTful wallet APIs using ResponseEntity and request mapping"
git commit -m "Introduce request and response DTOs with clean layered architecture"
git commit -m "Add bean validation for wallet creation requests"
git commit -m "Implement global exception handling with custom error responses"
git commit -m "Add externalized configuration and configuration properties support"
git commit -m "Add structured logging using SLF4J and Logback"
git commit -m "Add unit and controller testing examples using JUnit and Mockito"
```

------------------------------------------------------------------------

# Next Repository

`02-data-access`

Topics: - Spring Data JPA - Hibernate - MySQL - Transactions - Entity
Relationships - Flyway - Optimistic Locking - Pagination - Sorting -
Production Database Practices
