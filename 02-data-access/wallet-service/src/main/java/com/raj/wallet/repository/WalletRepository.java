package com.raj.wallet.repository;

import com.raj.wallet.entity.WalletEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {
    Page<WalletEntity> findAllBy(Pageable pageable);
}
