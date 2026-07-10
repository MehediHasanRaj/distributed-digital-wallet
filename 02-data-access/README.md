# Spring Data JPA & Hibernate Notes

> A practical learning guide for Spring Boot, Spring Data JPA and Hibernate.

## Topics Covered

- Spring Data JPA Setup
- MySQL + Docker
- Entity Lifecycle
- CRUD Repository
- Persistence Context
- Dirty Checking
- First Level Cache
- Transactions (ACID)
- Entity Relationships
- Cascade & Fetch Types
- Pagination & Sorting
- JPQL vs Native SQL
- Spring Data Auditing
- Flyway Database Migrations
- Optimistic Locking
- JPA Performance Best Practices
- Database Indexes
- N+1 Query Problem
- Fetch Join
- Entity Graph
- Batch Fetching
- LazyInitializationException

---
# Spring Data JPA + MySQL Notes

## Lesson 1 – Theory

### Persistence
**Persistence** means data continues to exist even after the application stops running.

---

# Lesson 2 – Setting Up MySQL & Spring Data JPA

## Install MySQL with Docker

```bash
docker run -d \
  --name wallet-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=walletdb \
  -p 3306:3306 \
  mysql:8.4
```

---

## Required Dependencies

### Spring Data JPA

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

### MySQL Driver

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>
```

---

## Why JDBC?

Java cannot communicate with MySQL directly.

```
Java
   │
   ▼
JDBC Driver
   │
   ▼
MySQL Database
```

The JDBC Driver acts as a translator between Java and MySQL.

---

## Configure Database (`application.properties`)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/walletdb
spring.datasource.username=root
spring.datasource.password=root

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

### Configuration Explanation

| Property | Purpose |
|----------|----------|
| `spring.datasource.url` | Database connection URL |
| `spring.datasource.username` | Database username |
| `spring.datasource.password` | Database password |
| `ddl-auto=update` | Automatically updates database schema |
| `show-sql=true` | Prints SQL queries in the console |
| `format_sql=true` | Formats SQL for readability |
| `database-platform` | Specifies Hibernate dialect |

> **Note:** Avoid using `ddl-auto=update` in production.

---

# Lesson 3 – JPA Entities

## UUID Primary Key

```java
@GeneratedValue(strategy = GenerationType.UUID)
```

---

## Generation Strategies

| Strategy | Example | Best For |
|-----------|---------|----------|
| `IDENTITY` | 1, 2, 3... | MySQL (Simple applications) |
| `SEQUENCE` | 100, 101... | PostgreSQL, Oracle |
| `TABLE` | 15, 16... | Rarely used |
| `AUTO` | Hibernate decides | General purpose |
| `UUID` | `550e8400-e29b-41d4-a716-446655440000` | Microservices, Distributed Systems, Public APIs |

---

## Constructors in JPA Entities

Every entity **must have a no-argument constructor**.

Example:

```java
public WalletEntity() {
}
```

### Why?

When Hibernate retrieves an entity from the database, it:

1. Creates an empty object.
2. Populates its fields.
3. Returns the populated object.

Without a no-args constructor, Hibernate cannot instantiate the entity.

Parameterized constructors are optional.

---

# Entity Lifecycle

```
Transient
     │
     ▼
Persistent (Managed)
     │
     ▼
Detached
     │
     ▼
Removed
```

## 1. Transient

- Exists only in Java memory.
- Not managed by Hibernate.
- Not stored in the database.

```java
WalletEntity wallet = new WalletEntity();
```

---

## 2. Persistent (Managed)

- Managed by Hibernate.
- Stored inside the Persistence Context.
- Automatically synchronized with the database.

```java
entityManager.persist(wallet);
```

---

## 3. Detached

- Entity still exists in memory.
- Hibernate no longer tracks changes.

```java
entityManager.close();
```

---

## 4. Removed

- Marked for deletion.
- Deleted from the database when the transaction commits.

```java
entityManager.remove(wallet);
```

---

# Repository

Create a repository by extending `JpaRepository`.

```java
public interface WalletRepository
        extends JpaRepository<WalletEntity, UUID> {

}
```

**Benefits**

- Save
- Find by ID
- Find all
- Delete
- Update
- Pagination
- Sorting

No implementation required.

---

# CRUD Operations

Typical CRUD operations:

- Create
- Read
- Update
- Delete

For DELETE requests, return:

```
HTTP 204 No Content
```

---

# DTO Mapping

Never expose your Entity directly.

Instead, use DTOs.

```
Client
   │
   ▼
DTO
   │
Mapping
   │
   ▼
Entity
   │
Database
```

Common mappings:

- Entity → DTO
- DTO → Entity

Benefits:

- Security
- Flexibility
- Better API design
- Prevent exposing internal database structure

---

# Hibernate Internals

## Architecture

```
Application
      │
      ▼
Spring Data JPA
      │
      ▼
EntityManager
      │
      ▼
Persistence Context
      │
      ▼
Hibernate
      │
      ▼
MySQL
```

---

## Persistence Context

The **Persistence Context** is a temporary workspace where Hibernate stores every managed entity loaded from the database.

Example:

```java
WalletEntity wallet = repository.findById(id).get();
```

Now the `wallet` object is inside the Persistence Context.

Hibernate tracks every change made to it.

---

## Dirty Checking

Once an entity is managed, Hibernate automatically detects modifications.

Example:

```java
WalletEntity wallet = repository.findById(id).get();

wallet.setBalance(199);
```

Notice:

```java
repository.save(wallet);
```

is **not required** (inside the same transaction).

Hibernate compares the entity before and after modification.

If changes are detected (the entity is "dirty"), Hibernate automatically executes:

```sql
UPDATE wallet
SET balance = 199
WHERE id = ...
```

This automatic change detection is called **Dirty Checking**.

---

## EntityManager

Everything in JPA revolves around the **EntityManager**.

The EntityManager is responsible for:

- Persisting entities
- Finding entities
- Removing entities
- Managing the Persistence Context
- Tracking changes (Dirty Checking)

Spring Data JPA hides most of the EntityManager, making database operations much simpler.

---

# EntityManager Deep Dive

## EntityManager Responsibilities

The **EntityManager** is the core interface in JPA responsible for managing the lifecycle of entities.

### Main Responsibilities

- Persist Entity
- Find Entity
- Remove Entity
- Merge Entity
- Manage the Persistence Context

> **Interview Tip:** Think of the **EntityManager** as Hibernate's manager. Almost everything Hibernate does revolves around it.

---

# First-Level Cache (Persistence Context)

One of the most common interview topics.

When an entity is loaded for the first time:

```java
walletRepository.findById(1L);
```

Flow:

```
Application
      │
      ▼
Hibernate
      │
      ▼
SQL Query
      │
      ▼
Database
      │
      ▼
Persistence Context (First-Level Cache)
```

Hibernate executes a SQL query and stores the entity inside the **Persistence Context**.

Now, if you execute the same query again within the same transaction:

```java
walletRepository.findById(1L);
```

Hibernate checks the Persistence Context first.

Since the entity is already managed, it returns the existing object directly.

**No SQL query is executed.**

This behavior is called the **First-Level Cache**.

### Example

```java
WalletEntity wallet1 = repository.findById(id).get();
WalletEntity wallet2 = repository.findById(id).get();
```

Result:

- First call → SQL executes.
- Second call → Returned from Persistence Context.
- No additional SQL query.

---

# Flush vs Commit

These two concepts are often confused.

## Flush

`flush()` synchronizes the Persistence Context with the database.

```java
wallet.setBalance(110);

entityManager.flush();
```

What happens?

- Hibernate generates and sends the SQL `UPDATE`.
- The transaction is **still open**.
- Changes can still be rolled back.
- Entity remains managed.

```
Modify Entity
      │
      ▼
flush()
      │
      ▼
SQL Sent to Database
(Transaction still active)
```

---

## Commit

```java
transaction.commit();
```

What happens?

- Transaction completes successfully.
- Changes become permanent.
- Data is committed to the database.
- Rollback is no longer possible.

```
flush()
      │
      ▼
commit()
      │
      ▼
Permanent Database Change
```

---

## Flush vs Commit Comparison

| Flush | Commit |
|--------|---------|
| Sends SQL to the database | Permanently saves the transaction |
| Transaction remains active | Transaction ends |
| Can still roll back | Cannot roll back after commit |
| Synchronizes Persistence Context | Finalizes all changes |

### Interview Question

> **Is `flush()` the same as `commit()`?**

**Answer:** No.

- **Flush** sends SQL statements to the database.
- **Commit** permanently saves those changes.

---

# persist()

```java
entityManager.persist(wallet);
```

What does `persist()` do?

- Marks the entity as **Managed**.
- Adds it to the Persistence Context.
- Hibernate schedules an `INSERT`.
- Actual SQL is executed during **flush** or **commit**.

Example:

```java
WalletEntity wallet = new WalletEntity();

entityManager.persist(wallet);

// INSERT has not been executed yet.
```

Later:

```java
entityManager.flush();
```

or

```java
transaction.commit();
```

Hibernate finally executes:

```sql
INSERT INTO wallet (...)
VALUES (...);
```

---

# Common Mistakes

## 1. Calling `save()` after every setter

Incorrect:

```java
wallet.setBalance(100);
repository.save(wallet);

wallet.setName("Raj");
repository.save(wallet);

wallet.setCurrency("USD");
repository.save(wallet);
```

If the entity is already managed, Hibernate automatically tracks changes.

Correct:

```java
wallet.setBalance(100);
wallet.setName("Raj");
wallet.setCurrency("USD");

// No save() required (inside the same transaction)
```

Hibernate performs **Dirty Checking** and updates the entity automatically.

---

## 2. Thinking `flush()` equals `commit()`

Incorrect understanding:

```
flush() == commit()
```

Correct understanding:

```
flush()
     ↓
SQL sent to database

commit()
     ↓
Transaction permanently saved
```

Remember:

- `flush()` → SQL sent.
- `commit()` → Transaction completed.

---

# Complete Entity Lifecycle

```
Database
    │
    ▼
find()
    │
    ▼
Managed Entity
    │
Modify Entity
    │
    ▼
Dirty Checking
    │
    ▼
flush()
    │
    ▼
SQL UPDATE Executed
    │
    ▼
commit()
    │
    ▼
Database Updated Permanently
```

---

# Quick Revision

### EntityManager

- Persist entities
- Find entities
- Remove entities
- Merge entities
- Manage the Persistence Context

### First-Level Cache

- Built into the Persistence Context.
- Same entity loaded twice → only one SQL query.
- Subsequent lookups return the managed entity from memory.

### Flush

- Sends SQL to the database.
- Transaction is still active.
- Changes can still be rolled back.

### Commit

- Permanently saves the transaction.
- Ends the transaction.
- Changes cannot be rolled back afterward.

### persist()

- Marks an entity as **Managed**.
- `INSERT` is executed during **flush** or **commit**.

### Common Interview Questions

- What is the Persistence Context?
- What is the First-Level Cache?
- Difference between `flush()` and `commit()`?
- What does `persist()` do?
- Why don't we need `save()` after modifying a managed entity?
- Explain Dirty Checking.

# Lesson 4 – Transactions

## What is a Transaction?

A **transaction** is a group of database operations that are treated as a single unit of work.

> **Rule:** Either **all operations succeed** or **none of them do**.

If any operation fails, the entire transaction is **rolled back**.

Example:

```
Transfer Money

Account A (-100)
        │
        ▼
Account B (+100)
```

If updating **Account B** fails, the deduction from **Account A** is also undone.

---

# ACID Properties

Transactions follow the **ACID** principles.

| Property | Meaning |
|----------|---------|
| **A - Atomicity** | All operations succeed or all fail (all or nothing). |
| **C - Consistency** | The database always remains in a valid state before and after the transaction. |
| **I - Isolation** | Multiple transactions should not interfere with each other's data. |
| **D - Durability** | Once committed, data survives crashes such as power or server failures. |

---

# Spring Transaction Management

Spring simplifies transaction management using the `@Transactional` annotation.

```java
@Transactional
public void transferMoney() {

}
```

When a method is marked with `@Transactional`, Spring automatically:

- Starts a transaction.
- Executes the method.
- Commits the transaction if everything succeeds.
- Rolls back the transaction if an exception occurs.

```
Method Starts
      │
      ▼
Transaction Begins
      │
      ▼
Business Logic
      │
      ▼
Success?
 ┌─────────────┐
 │ Yes         │
 ▼             ▼
Commit      Rollback
```

> **Interview Tip:** `@Transactional` is one of the most frequently asked Spring annotations.

---

# Lesson 5 – Entity Relationships

Entities often relate to one another. JPA provides annotations to model these relationships.

---

# One-to-One (1:1)

One entity is associated with exactly one other entity.

Example:

- One User → One User Profile

```java
@OneToOne
private UserProfile profile;
```

```
User
 │
 │
 ▼
UserProfile
```

---

# One-to-Many (1:N)

One parent entity has multiple child entities.

**Most common relationship in Spring Boot applications.**

Example:

- One User → Many Wallets

### User Side

```java
@OneToMany(mappedBy = "user")
private List<WalletEntity> wallets;
```

- `mappedBy = "user"` tells Hibernate that the `user` field in `WalletEntity` owns the relationship.
- No extra join table is created.

### Wallet Side

```java
@ManyToOne
@JoinColumn(name = "user_id")
private UserEntity user;
```

`@JoinColumn` creates the foreign key:

```
wallet
-------------------------
id
balance
user_id   ← Foreign Key
```

---

# Many-to-One (N:1)

Many child entities belong to one parent.

Example:

Many Transactions → One Wallet

```java
@ManyToOne
@JoinColumn(name = "wallet_id")
private WalletEntity wallet;
```

```
Transaction
      │
      │
      ▼
Wallet
```

---

# Many-to-Many (N:N)

Many entities relate to many others.

Example:

Users ↔ Roles

```java
@ManyToMany
private Set<RoleEntity> roles;
```

```
User
  ▲
  │
Join Table
  │
  ▼
Role
```

Hibernate creates a **join table** to manage this relationship.

---

# Cascade Types

Cascade determines whether operations performed on the parent entity should automatically apply to its child entities.

Example:

Should saving a `User` automatically save its `Wallets`?

```
User
 │
 ├── Wallet 1
 ├── Wallet 2
 └── Wallet 3
```

---

## Cascade.PERSIST

Saving the parent also saves its children.

```java
cascade = CascadeType.PERSIST
```

```
Save User
     │
     ▼
Wallets Saved Automatically
```

---

## Cascade.REMOVE

Deleting the parent also deletes its children.

```java
cascade = CascadeType.REMOVE
```

```
Delete User
      │
      ▼
Wallets Deleted Automatically
```

---

## Cascade.ALL

Applies all cascade operations:

- Persist
- Merge
- Remove
- Refresh
- Detach

```java
cascade = CascadeType.ALL
```

> **Use with caution.** It can unintentionally delete or modify related entities.

---

# Fetch Types

Fetch type determines **when related entities are loaded**.

## EAGER Fetch

Related entities are loaded immediately.

```java
@ManyToOne(fetch = FetchType.EAGER)
```

Example:

```
Load User
     │
     ▼
Load Wallets Immediately
```

**Default for:** `@ManyToOne`

---

## LAZY Fetch

Related entities are loaded only when accessed.

```java
@ManyToOne(fetch = FetchType.LAZY)
```

Example:

```
Load User
     │
     ▼
Wallets NOT Loaded Yet
     │
Access wallets
     ▼
Wallets Loaded
```

**Default for:** `@OneToMany`

> **Best Practice:** Avoid using `EAGER` everywhere. Prefer `LAZY` unless you specifically need related data immediately.

---

# orphanRemoval

`orphanRemoval = true` automatically deletes child entities that are removed from the parent's collection.

Example:

```java
@Entity
public class UserEntity {

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<WalletEntity> wallets;

}
```

If a wallet is removed from the user's wallet list:

```java
user.getWallets().remove(wallet);
```

Hibernate automatically executes:

```sql
DELETE FROM wallet
WHERE id = ...
```

Without `orphanRemoval`, the wallet would simply lose its relationship with the user but remain in the database.

---

# Example: LAZY Relationship

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private UserEntity user;
```

- `user_id` becomes the foreign key.
- User data is fetched only when needed.

---

# N+1 Query Problem

One of the most common Hibernate performance issues.

Example:

Suppose there are **10 users**.

```java
List<User> users = repository.findAll();
```

Hibernate executes:

```sql
SELECT * FROM users;
```

(1 query)

Later:

```java
user.getWallets();
```

for each user.

Hibernate executes:

```sql
SELECT * FROM wallet WHERE user_id = 1;
SELECT * FROM wallet WHERE user_id = 2;
SELECT * FROM wallet WHERE user_id = 3;
...
```

Total:

- 1 query to load users.
- 10 additional queries to load wallets.

```
1 + 10 = 11 Queries
```

This is called the **N+1 Query Problem**.

It can significantly reduce application performance.

Common solutions include:

- `JOIN FETCH`
- `@EntityGraph`
- DTO projections

---

# Quick Revision

### Transaction

- A group of operations executed as a single unit.
- All succeed or all fail.

### ACID

- **Atomicity** → All or nothing.
- **Consistency** → Database always remains valid.
- **Isolation** → Transactions do not interfere.
- **Durability** → Committed data survives failures.

### `@Transactional`

- Starts a transaction.
- Commits on success.
- Rolls back on failure.

### Relationships

| Relationship | Example |
|--------------|---------|
| One-to-One | User → Profile |
| One-to-Many | User → Wallets |
| Many-to-One | Transactions → Wallet |
| Many-to-Many | Users ↔ Roles |

### Cascade

- `PERSIST` → Save children automatically.
- `REMOVE` → Delete children automatically.
- `ALL` → Apply all cascade operations.

### Fetch Types

- **EAGER** → Load related entities immediately.
- **LAZY** → Load related entities only when needed.

### orphanRemoval

- Removing a child from the parent's collection deletes it from the database.

### N+1 Query Problem

- One query loads parent entities.
- Additional queries load each child collection.
- Example: 1 user query + 10 wallet queries = **11 total queries**.



# Lesson 6 – Pagination & Sorting

## Why Pagination?

Loading thousands of records at once is inefficient and slows down applications.

Instead, retrieve data in **smaller chunks (pages)**.

Example:

```
Database (10,000 Wallets)
          │
          ▼
Page 0 → 10 records

Page 1 → 10 records

Page 2 → 10 records
```

---

# Pageable

Spring Data JPA provides the `Pageable` interface for pagination.

```java
Pageable pageable =
        PageRequest.of(page, size, Sort.by("owner"));
```

### Parameters

| Parameter | Meaning |
|-----------|---------|
| `page` | Page number (starts at 0) |
| `size` | Number of records per page |
| `Sort.by()` | Sorting field |

Example:

```java
PageRequest.of(0, 5);
```

Returns:

- First page
- 5 records

---

# Sorting

Sort results by one or more fields.

```java
Sort.by("owner")
```

Descending order:

```java
Sort.by("balance").descending()
```

Multiple fields:

```java
Sort.by("owner")
    .and(Sort.by("balance").descending());
```

---

# List vs Page

## List

```java
List<WalletEntity>
```

Contains only the data.

```
[
 Wallet,
 Wallet,
 Wallet
]
```

No pagination information is included.

---

## Page

```java
Page<WalletEntity>
```

Contains:

- Data
- Page number
- Total pages
- Total elements
- Page size
- Whether it's the first/last page

```
Page
├── Content
├── Total Pages
├── Total Elements
├── Current Page
├── Page Size
└── First / Last
```

### Example

```java
Page<WalletEntity> wallets =
        repository.findAll(pageable);
```

Useful methods:

```java
wallets.getContent();

wallets.getTotalPages();

wallets.getTotalElements();

wallets.getNumber();

wallets.isFirst();

wallets.isLast();
```

> **Interview Tip:** `Page` contains both **data and metadata**, while `List` contains only data.

---

# Lesson 7 – JPQL (Java Persistence Query Language)

## Why JPQL?

Built-in CRUD methods are sufficient for simple operations.

When queries become more expressive or complex, use **JPQL**.

---

# Example

```sql
SELECT w
FROM WalletEntity w
WHERE w.balance > 5000
```

Notice:

- `WalletEntity` is the **entity class**, not the database table.
- `balance` is the **Java field**, not the database column.

JPQL works entirely with Java objects.

---

# SQL vs JPQL

## SQL

```sql
SELECT *
FROM wallets
WHERE balance > 5000;
```

Uses:

- Table names
- Column names

---

## JPQL

```sql
SELECT w
FROM WalletEntity w
WHERE w.balance > 5000
```

Uses:

- Entity names
- Java field names

---

# Creating Custom Queries

Use the `@Query` annotation inside the repository.

```java
@Query("""
SELECT w
FROM WalletEntity w
WHERE w.balance > :balance
""")
List<WalletEntity> findRichWallets(
        @Param("balance") BigDecimal balance);
```

### Named Parameters

```java
:balance
```

The `:` indicates a named parameter.

```java
@Param("balance")
```

binds the method parameter to the JPQL query.

---

# Aggregate Functions

JPQL supports aggregate functions similar to SQL.

Examples:

```sql
COUNT(w)
```

```sql
SUM(w.balance)
```

```sql
AVG(w.balance)
```

Other commonly used functions:

- `MIN()`
- `MAX()`

---

# Common JPQL Mistakes

### Using Table Names

❌ Incorrect

```sql
SELECT *
FROM wallets
```

✅ Correct

```sql
SELECT w
FROM WalletEntity w
```

---

### Using Column Names

❌ Incorrect

```sql
wallet_balance
```

✅ Correct

```sql
balance
```

Use the Java field name.

---

### Forgetting `@Modifying`

For update or delete queries:

```java
@Modifying
```

is required.

---

### Forgetting `@Transactional`

Update and delete JPQL queries should also use:

```java
@Transactional
```

---

# JPQL vs Native SQL

| JPQL | Native SQL |
|------|------------|
| Uses entities | Uses tables |
| Uses Java fields | Uses database columns |
| Database independent | Database specific |
| Recommended by default | Use only when necessary |

---

# Native SQL

Use native SQL only when you need database-specific features.

```java
@Query(
    value = "SELECT * FROM wallets",
    nativeQuery = true
)
List<Wallet> findAllWallets();
```

---

# JPQL Example

```java
@Query("""
SELECT w
FROM Wallet w
""")
List<Wallet> findAllWallets();
```

---

# Native SQL Example

```java
@Query(
    value = "SELECT * FROM wallets",
    nativeQuery = true
)
List<Wallet> findAllWallets();
```

> **Best Practice:** Prefer JPQL. Use Native SQL only for database-specific functionality (e.g., MySQL or PostgreSQL features).

---

# Lesson 8 – Spring Data JPA Auditing

## Why Auditing?

Auditing automatically records important information about database changes.

Examples:

- Who created this record?
- Who updated it?
- When was it created?
- When was it last modified?

```
Wallet

Created By
Created At
Updated By
Updated At
```

---

# Step 1 – Enable Auditing

Enable auditing in the main application.

```java
@EnableJpaAuditing
@SpringBootApplication
public class WalletApplication {

}
```

Without this annotation, auditing will not work.

---

# Step 2 – Create a Base Entity

Create a reusable parent class for audit fields.

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
```

---

# @MappedSuperclass

`@MappedSuperclass` tells JPA:

- This class is **not** a database entity.
- Other entities inherit its fields.

Example:

```java
public class WalletEntity
        extends BaseEntity {

}
```

Result:

```
WalletEntity

id
owner
balance
createdAt
updatedAt
```

The audit fields become part of `WalletEntity`.

---

# @EntityListeners

```java
@EntityListeners(AuditingEntityListener.class)
```

This registers the auditing listener.

Spring automatically listens for:

- Insert operations
- Update operations

Then it updates the audit fields.

```
Insert
   │
   ▼
createdAt set automatically

Update
   │
   ▼
updatedAt updated automatically
```

---

# AuditorAware

Spring does not know who the current user is.

You provide that information through `AuditorAware`.

```java
@Configuration
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {

        return () -> Optional.of("SYSTEM");

    }

}
```

Currently:

```
Created By

SYSTEM
```

Later, when authentication (e.g., Spring Security) is added, this can return the logged-in user's username instead.

---

# Common Auditing Annotations

| Annotation | Purpose |
|------------|---------|
| `@CreatedDate` | Automatically stores the creation timestamp |
| `@LastModifiedDate` | Automatically stores the last update timestamp |
| `@CreatedBy` | Stores who created the record |
| `@LastModifiedBy` | Stores who last modified the record |

Example:

```java
@CreatedBy
private String createdBy;

@LastModifiedBy
private String updatedBy;
```

---

# Common Mistakes

### Forgetting `@EnableJpaAuditing`

Auditing will never start.

---

### Forgetting `@EntityListeners`

Audit annotations will not be triggered.

---

### Duplicating Audit Fields

Do **not** repeat audit fields in every entity.

Instead, create one reusable `BaseEntity` and let other entities extend it.

---

### Updating `createdAt`

```java
@Column(updatable = false)
```

ensures the creation timestamp is never modified after insertion.

---

# Quick Revision

## Pagination

- Loads records in small chunks.
- Improves performance.

### Pageable

```java
PageRequest.of(page, size, Sort.by("owner"))
```

### List vs Page

| List | Page |
|------|------|
| Data only | Data + pagination metadata |

---

## JPQL

- Uses entity names.
- Uses Java field names.
- Database independent.
- Recommended by default.

### Native SQL

- Uses table names.
- Uses column names.
- Database specific.
- Use only when necessary.

---

## Auditing

Purpose:

- Track **who** created or updated a record.
- Track **when** it was created or modified.

Required setup:

- `@EnableJpaAuditing`
- `@MappedSuperclass`
- `@EntityListeners(AuditingEntityListener.class)`
- `AuditorAware`

Common audit annotations:

- `@CreatedDate`
- `@LastModifiedDate`
- `@CreatedBy`
- `@LastModifiedBy`
````
````
# Lesson 9 – Flyway Database Migrations

## Why Flyway?

Managing database schema changes manually becomes difficult as an application grows.

**Flyway** provides **database version control** by managing schema changes through versioned migration scripts.

It ensures:

- Every environment (Development, Testing, Production) has the same schema.
- Database changes are applied in the correct order.
- Every migration is tracked.
- Database history is preserved.

> **Think of it this way:**
>
> - **Git** tracks **code changes**.
> - **Flyway** tracks **database changes**.

---

# Why Not `ddl-auto=update`?

Using:

```properties
spring.jpa.hibernate.ddl-auto=update
```

lets Hibernate automatically modify the database schema.

While convenient during development, it is **not recommended for production** because:

- Schema changes are not version controlled.
- Team members may have different schemas.
- No history of database changes.
- Risk of unexpected schema modifications.

---

# How to Use Flyway

## Step 1 – Add Dependency

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

---

## Step 2 – Configure Hibernate

Instead of:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Use:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

### Why?

Hibernate now **validates** the database schema, while Flyway is responsible for creating and updating it.

```
Flyway
   │
Creates / Updates Schema
   │
   ▼
Hibernate
   │
Validates Schema
```

> **Production Best Practice:**  
> Flyway manages schema changes. Hibernate only validates them.

---

# Migration Naming Convention

Each migration file follows this format:

```text
V1__Create_wallet_table.sql
```

Examples:

```text
V1__Create_user_table.sql

V2__Create_wallet_table.sql

V3__Add_balance_column.sql

V4__Create_transaction_table.sql
```

Naming format:

```
V<Version>__<Description>.sql
```

---

# Flyway Migration Process

Application startup flow:

```
Application
      │
      ▼
Flyway
      │
      ▼
Run SQL Migrations
      │
      ▼
Hibernate Validate
      │
      ▼
Repositories Ready
      │
      ▼
Application Starts
```

> **Important:** Flyway always runs **before** Hibernate.

---

# Flyway History Table

When Flyway runs for the first time, it automatically creates:

```text
flyway_schema_history
```

This table stores:

- Migration version
- Migration description
- Execution time
- Success status
- Checksum

Example:

| Version | Description | Status |
|----------|-------------|--------|
| V1 | Create wallet table | Success |
| V2 | Add balance column | Success |
| V3 | Create transaction table | Success |

Flyway checks this table every time the application starts.

If a migration has already been applied, Flyway **will not run it again**.

---

# Flyway Validation

Every migration has a **checksum**.

If someone edits a migration after it has already been applied:

```
V1__Create_wallet_table.sql
```

Flyway detects that its checksum has changed.

Result:

```
Checksum Mismatch
```

The application **fails to start**.

This protects production databases from accidental modifications.

---

# Roll Forward Strategy

Never edit or delete an old migration that has already been executed.

Instead:

Create a **new migration** that fixes the issue.

Example:

```
V1__Create_wallet_table.sql

❌ Edit V1

✅ Create

V2__Fix_wallet_table.sql
```

This approach is called the **Roll Forward Strategy**.

---

# Common Flyway Mistakes

### Using `ddl-auto=update`

```properties
ddl-auto=update
```

Not recommended for production.

Use:

```properties
ddl-auto=validate
```

---

### Editing Old Migrations

Never modify an already executed migration.

Create a new migration instead.

---

### Skipping Version Numbers

Incorrect:

```
V1

V3
```

Correct:

```
V1

V2

V3
```

Migration versions should be sequential.

---

# Lesson 10 – Optimistic Locking

## Problem: Lost Update

Imagine two users updating the same wallet at the same time.

Example:

```
Wallet Balance

100
```

### User A

Reads balance:

```
100
```

---

### User B

Also reads:

```
100
```

---

User A updates:

```
150
```

User B updates:

```
200
```

Final balance becomes:

```
200
```

User A's update is lost.

This is called the **Lost Update Problem**.

---

# Solution: Optimistic Locking

Optimistic Locking prevents lost updates by adding a **version number** to every row.

```java
@Version
private Long version;
```

Hibernate automatically manages this field.

You never update it manually.

---

# How It Works

Suppose the current row is:

| Balance | Version |
|----------|----------|
| 100 | 5 |

User A and User B both read:

```
Version = 5
```

User A updates first.

Hibernate executes:

```sql
UPDATE wallet
SET balance = 150,
    version = 6
WHERE id = 1
AND version = 5;
```

Success.

Database now contains:

| Balance | Version |
|----------|----------|
| 150 | 6 |

---

User B now tries to update.

Hibernate executes:

```sql
UPDATE wallet
SET balance = 200,
    version = 6
WHERE id = 1
AND version = 5;
```

No rows match because the version is already **6**.

Hibernate throws an **Optimistic Lock Exception**.

This prevents User A's update from being overwritten.

---

# Optimistic Lock Flow

```
Read Entity
      │
      ▼
Version = 5
      │
      ▼
Modify Entity
      │
      ▼
UPDATE
WHERE version = 5
      │
      ▼
Success
      │
      ▼
Version becomes 6
```

---

# When Version Is Checked

| Operation | Version Checked? |
|------------|------------------|
| SELECT | ❌ No |
| UPDATE | ✅ Yes |
| DELETE | ✅ Yes |

Version checking is only performed when data is modified or deleted.

---

# Pessimistic Locking

An alternative approach is **Pessimistic Locking**.

Instead of detecting conflicts later, it locks the database row immediately.

```
Read Row
     │
     ▼
Lock Row
     │
     ▼
Other Transactions Must Wait
```

---

# Optimistic vs Pessimistic Locking

| Optimistic Locking | Pessimistic Locking |
|--------------------|---------------------|
| No database lock | Locks the database row |
| Better concurrency | Lower concurrency |
| Detects conflicts during update | Prevents conflicts before update |
| Better scalability | Slower under heavy locking |
| Best for most web applications | Best for high-contention scenarios |

---

# Which Should Our Wallet System Use?

✅ **Optimistic Locking**

Reason:

Most wallet records are **not updated simultaneously**.

Benefits:

- Better scalability
- Higher concurrency
- Better performance
- Less database locking

Pessimistic locking should only be used when many users frequently update the same records.

---

# Common Mistakes

### Forgetting `@Version`

Without it, Hibernate cannot perform optimistic locking.

---

### Updating the Version Manually

Never modify:

```java
version
```

Hibernate manages it automatically.

---

### Ignoring the Exception

When an optimistic locking conflict occurs, handle the exception appropriately (e.g., notify the user or retry the operation).

---

### Using Pessimistic Locking Everywhere

Excessive locking reduces concurrency and hurts performance.

Use pessimistic locking only when truly necessary.

---

# Interview Questions

### 1. What problem does Optimistic Locking solve?

It prevents **lost updates** by detecting concurrent modifications before committing changes.

---

### 2. How does Hibernate implement Optimistic Locking?

Hibernate uses a field annotated with:

```java
@Version
```

During `UPDATE` or `DELETE`, Hibernate includes the version value in the SQL `WHERE` clause.

If the version no longer matches, Hibernate throws an **Optimistic Lock Exception**.

---

# Quick Revision

## Flyway

- Database version control tool.
- Tracks schema changes with versioned SQL scripts.
- Runs before Hibernate.
- Creates the `flyway_schema_history` table.
- Uses `ddl-auto=validate` with Hibernate.
- Never edit old migrations—create a new one (**Roll Forward Strategy**).

---

## Optimistic Locking

- Solves the **Lost Update Problem**.
- Uses `@Version` to track row versions.
- Hibernate checks the version during `UPDATE` and `DELETE`.
- Throws an exception if another transaction has already modified the row.
- Recommended for most web applications due to better scalability and concurrency.
````
````
# Lesson 11 – JPA Performance & Best Practices

## Goal

By the end of this lesson, you should understand:

- Database Indexes
- N+1 Query Problem
- Fetch Join
- Entity Graph
- Batch Fetching
- LazyInitializationException
- Query Optimization
- Hibernate Statistics
- Performance Best Practices

---

# Database Indexes

## What is an Index?

Imagine reading a **1000-page book**.

Without an index:

- You scan every page until you find **Chapter 18**.

With an index:

- You jump directly to **Chapter 18**.

Databases work the same way.

```
Without Index

Database
    │
Scan Every Row
    │
Find Result

Slow
```

```
With Index

Database
    │
Use Index
    │
Jump Directly
    │
Find Result

Fast
```

Indexes dramatically improve **read performance**, especially for large tables.

---

# Creating an Index

JPA provides the `@Index` annotation.

```java
@Table(
    name = "users",
    indexes = {
        @Index(
            name = "idx_email",
            columnList = "email"
        )
    }
)
```

Hibernate generates an index for the `email` column.

---

# When to Use Indexes

Good candidates:

- Email
- Username
- Foreign keys
- Frequently searched columns

Example:

```sql
SELECT *
FROM users
WHERE email = 'raj@example.com';
```

The index helps the database locate the record quickly.

---

# Don't Index Everything

Indexes improve:

- SELECT

But slightly slow down:

- INSERT
- UPDATE
- DELETE

Why?

Because the database must maintain the index whenever data changes.

> **Best Practice:** Add indexes only to columns that are frequently searched, filtered, or joined.

---

# N+1 Query Problem

One of the most common Hibernate performance issues.

Suppose:

- 10 Users
- Each user has 5 Wallets

```java
List<UserEntity> users = repository.findAll();
```

Hibernate executes:

```sql
SELECT * FROM users;
```

(1 query)

Then:

```java
user.getWallets();
```

for every user.

Hibernate executes:

```sql
SELECT * FROM wallets WHERE user_id = 1;

SELECT * FROM wallets WHERE user_id = 2;

...

SELECT * FROM wallets WHERE user_id = 10;
```

Total:

```
1 + 10 = 11 Queries
```

This is called the **N+1 Query Problem**.

---

# Solution 1 – Fetch Join

A **Fetch Join** loads parent and child entities in a single SQL query.

```java
@Query("""
SELECT u
FROM UserEntity u
JOIN FETCH u.wallets
""")
List<UserEntity> findAllUsersWithWallets();
```

Result:

```
Users
      │
JOIN
      │
Wallets

One SQL Query
```

Advantages:

- Excellent performance
- Full control over the query
- Best for complex joins

---

# Solution 2 – Entity Graph

Instead of writing JPQL, let Spring handle eager loading for selected relationships.

```java
@EntityGraph(attributePaths = {"wallets"})
List<UserEntity> findAll();
```

Spring automatically fetches the `wallets` association.

Advantages:

- Cleaner code
- No JPQL required
- Great for repository methods

---

# Fetch Join vs Entity Graph

| Fetch Join | Entity Graph |
|------------|--------------|
| Uses JPQL | Uses annotations |
| More control | Cleaner and easier to maintain |
| Best for complex joins | Best for repository methods |

---

# Batch Fetching

Imagine loading **100 wallets**.

Without batch fetching:

```
100 SQL Queries
```

Hibernate can group these requests.

Configuration:

```properties
spring.jpa.properties.hibernate.default_batch_fetch_size=50
```

Now Hibernate loads:

```
Query 1

50 Wallets

↓

Query 2

50 Wallets
```

Instead of:

```
100 Queries
```

You get:

```
2 Queries
```

This greatly improves performance.

---

# LazyInitializationException

One of the most common Hibernate exceptions.

## What Causes It?

A lazily loaded relationship is accessed **after the Persistence Context has been closed**.

Example:

```java
User user =
        userRepository.findById(1L).get();

System.out.println(user.getOrders());
```

Exception:

```
LazyInitializationException
```

Entity:

```java
@Entity
class User {

    @OneToMany(
        mappedBy = "user",
        fetch = FetchType.LAZY
    )
    private List<Order> orders;

}
```

Since `orders` is **LAZY**, Hibernate does not load it immediately.

If the Persistence Context is already closed, Hibernate can no longer fetch the data.

---

# Why Not Just Use EAGER?

Changing everything to:

```java
fetch = FetchType.EAGER
```

is **not recommended**.

Why?

- Loads unnecessary data.
- More memory usage.
- Slower queries.
- Can create new N+1 problems.

---

# Solutions to LazyInitializationException

## 1. `@Transactional` (Most Common)

Keep the Persistence Context open while accessing lazy relationships.

```java
@Transactional
public User getUser() {

}
```

---

## 2. Fetch Join (Best Practice)

Load related entities in one query.

```java
JOIN FETCH
```

---

## 3. Entity Graph

```java
@EntityGraph(attributePaths = {"orders"})
```

Spring loads the required association automatically.

---

# Query Optimization

Always filter data **inside the database**, not in Java.

### ❌ Bad

```java
List<User> users = repository.findAll();

users.stream()
     .filter(user -> user.getAge() > 18);
```

All rows are loaded into memory before filtering.

---

### ✅ Good

```java
SELECT u
FROM UserEntity u
WHERE u.age > 18
```

The database returns only the required rows.

> **Best Practice:** Let the database do the filtering whenever possible.

---

# Hibernate Statistics

Hibernate provides built-in statistics to analyze performance.

Useful for identifying:

- Number of SQL queries
- Cache usage
- Entity loading
- Query execution times

Enable statistics:

```properties
spring.jpa.properties.hibernate.generate_statistics=true
```

These metrics help identify bottlenecks and optimize application performance.

---

# Performance Best Practices

### ✅ Use indexes on frequently searched columns.

### ✅ Prefer `LAZY` loading over `EAGER`.

### ✅ Solve N+1 problems with:

- Fetch Join
- Entity Graph

### ✅ Enable batch fetching for collections.

### ✅ Filter data in SQL instead of Java.

### ✅ Retrieve only the data you need.

### ✅ Monitor Hibernate statistics during development.

### ❌ Don't use `FetchType.EAGER` everywhere.

### ❌ Don't load thousands of records without pagination.

### ❌ Don't fetch unnecessary columns or relationships.

---

# Quick Revision

## Database Index

- Speeds up searches.
- Similar to a book's index.
- Improves reads.
- Slightly slows inserts, updates, and deletes.

---

## N+1 Query Problem

- One query loads parent entities.
- Additional queries load each child collection.
- Solved using:
  - Fetch Join
  - Entity Graph

---

## Fetch Join

- Uses JPQL.
- Loads related entities in a single query.
- Best for complex joins.

---

## Entity Graph

- Uses annotations.
- Cleaner than JPQL.
- Ideal for repository methods.

---

## Batch Fetching

```properties
spring.jpa.properties.hibernate.default_batch_fetch_size=50
```

Loads entities in batches instead of one at a time.

---

## LazyInitializationException

Occurs when a lazy association is accessed after the Persistence Context has been closed.

Common solutions:

- `@Transactional`
- Fetch Join
- Entity Graph

---

## Query Optimization

- Filter data in SQL.
- Avoid loading unnecessary rows.
- Let the database do the heavy work.

---

## Hibernate Statistics

```properties
spring.jpa.properties.hibernate.generate_statistics=true
```

Use statistics to measure query counts, cache usage, and overall JPA performance.
````
