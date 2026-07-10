package com.raj.wallet.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<WalletEntity> wallets;

}

