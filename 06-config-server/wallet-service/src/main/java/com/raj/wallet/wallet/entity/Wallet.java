package com.raj.wallet.wallet.entity;

import com.raj.wallet.common.entity.BaseEntity;
import com.raj.wallet.wallet.enums.Currency;
import com.raj.wallet.wallet.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "wallets", indexes = {
                @Index(name = "idx_wallet_id", columnList = "wallet_id", unique = true),
                @Index(name = "idx_user_id", columnList = "user_id")

        }
)

public class Wallet extends BaseEntity {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "wallet_id",
            nullable = false,
            unique = true,
            updatable = false
    )
    private UUID walletId;

    @Column(
            name = "user_id",
            nullable = false,
            updatable = false
    )
    private UUID userId;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletStatus status;

    @Version
    private Long version; // that will

    @PrePersist  // this method run after creating object to initialize the object
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

}