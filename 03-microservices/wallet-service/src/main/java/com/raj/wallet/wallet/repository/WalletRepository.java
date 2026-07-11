package com.raj.wallet.wallet.repository;

import com.raj.wallet.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByWalletId(UUID walletId);
}
