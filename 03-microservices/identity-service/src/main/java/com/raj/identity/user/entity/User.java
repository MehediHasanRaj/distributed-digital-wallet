package com.raj.identity.user.entity;

import com.raj.identity.common.entity.BaseEntity;
import com.raj.identity.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users", indexes = {@Index(
                        name = "idx_user_id",
                        columnList = "user_id",
                        unique = true
                ),
                @Index(
                        name = "idx_email",
                        columnList = "email",
                        unique = true
                )
        }
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "user_id",
            nullable = false,
            unique = true,
            updatable = false
    )
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {

        if (userId == null) {
            userId = UUID.randomUUID();
        }

        if (status == null) {
            status = UserStatus.ACTIVE;
        }

    }

}