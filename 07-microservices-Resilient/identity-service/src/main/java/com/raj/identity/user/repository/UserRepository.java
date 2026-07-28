package com.raj.identity.user.repository;

import com.raj.identity.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByUserId(UUID userId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

}