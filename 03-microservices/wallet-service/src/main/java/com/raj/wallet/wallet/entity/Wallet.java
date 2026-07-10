package com.raj.wallet.wallet.entity;

import com.raj.wallet.wallet.enums.WalletStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

@Entity

@Table(name="wallets")
public class Wallet {

    @Id

    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private UUID userId;  // that is user id from another service

    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    private WalletStatus status;

    @Version   //to prevent optimistic locking
    private Long version;

}