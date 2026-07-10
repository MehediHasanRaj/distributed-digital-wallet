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

# 1. Spring Data JPA

Spring Data JPA simplifies database access by providing repositories on top of JPA/Hibernate.

## Setup

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/walletdb
spring.datasource.username=root
spring.datasource.password=root

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Docker:

```bash
docker run -d \
--name wallet-mysql \
-e MYSQL_ROOT_PASSWORD=root \
-e MYSQL_DATABASE=walletdb \
-p 3306:3306 \
mysql:8.4
```

---

# 2. Entity Lifecycle

```
Transient
    ↓
Persistent (Managed)
    ↓
Detached
    ↓
Removed
```

Managed entities are tracked automatically by Hibernate.

---

# 3. Dirty Checking

Hibernate automatically detects changes to managed entities.

```java
wallet.setBalance(500);
```

No need to call `save()` again inside a transaction.

---

# 4. First Level Cache

```
findById()
        ↓
Persistence Context
        ↓
Second findById()
        ↓
No SQL executed
```

---

# 5. Transactions

```java
@Transactional
public void transferMoney() {

}
```

ACID

- Atomicity
- Consistency
- Isolation
- Durability

---

# 6. Entity Relationships

## One To One

```java
@OneToOne
private UserProfile profile;
```

## One To Many

```java
@OneToMany(mappedBy="user")
private List<WalletEntity> wallets;
```

## Many To One

```java
@ManyToOne
@JoinColumn(name="user_id")
private UserEntity user;
```

## Many To Many

```java
@ManyToMany
private Set<RoleEntity> roles;
```

---

# 7. Pagination

```java
Pageable pageable =
PageRequest.of(page,size,Sort.by("owner"));
```

## Pageable vs Page

| Pageable | Page |
|-----------|------|
| Request | Response |
| Input | Output |
| Tells Spring what to fetch | Contains data + metadata |

---

# 8. JPQL vs Native SQL

JPQL

```java
@Query("SELECT w FROM WalletEntity w")
```

Native SQL

```java
@Query(value="SELECT * FROM wallets", nativeQuery=true)
```

| JPQL | Native SQL |
|------|------------|
| Entity names | Table names |
| Database independent | Database specific |

---

# 9. Auditing

```java
@EnableJpaAuditing
```

```java
@CreatedDate
private LocalDateTime createdAt;

@LastModifiedDate
private LocalDateTime updatedAt;
```

---

# 10. Flyway

## Why?

- Version control for database
- Keeps every environment synchronized
- Prevents schema drift

Migration naming

```
V1__Create_wallet_table.sql
V2__Add_currency.sql
```

Execution order

```
Application
    ↓
Flyway
    ↓
Run Migration
    ↓
Hibernate Validate
    ↓
Application Ready
```

---

# 11. Optimistic Locking

```java
@Version
private Long version;
```

```
Read
 ↓
Version = 5
 ↓
Update
 ↓
WHERE version=5
 ↓
Success
 ↓
Version=6
```

---

# 12. Performance

## Database Index

```java
@Table(
indexes={
@Index(name="idx_email",
columnList="email")
}
)
```

Index good candidates

- Email
- Username
- Foreign Keys
- Frequently searched columns
- ORDER BY columns
- JOIN columns

Avoid indexing

- Boolean fields
- Tiny tables
- Frequently updated columns

---

# 13. N+1 Query

Problem

```
1 Query Users
+
10 Queries Wallets
=
11 Queries
```

Solutions

- JOIN FETCH
- EntityGraph
- Batch Fetching

---

# 14. LazyInitializationException

Cause

Accessing a lazy association after the Hibernate Session is closed.

Solutions

- @Transactional
- JOIN FETCH
- @EntityGraph
- DTO mapping inside transaction

Avoid making everything EAGER.

---

# Interview Favourites

- Entity Lifecycle
- Dirty Checking
- Persistence Context
- First Level Cache
- Flush vs Commit
- Cascade Types
- Fetch Types
- N+1 Problem
- LazyInitializationException
- JPQL vs Native SQL
- Flyway
- Optimistic Locking
- Pageable vs Page
- Database Indexes

---

## Best Practices

- Prefer LAZY fetching.
- Avoid `ddl-auto=update` in production.
- Use Flyway migrations.
- Use DTOs.
- Use JOIN FETCH when required.
- Handle OptimisticLockException.
- Create indexes only when justified.
- Prefer JPQL unless native SQL is required.
