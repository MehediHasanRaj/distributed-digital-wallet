package com.raj.wallet.service;

import com.raj.wallet.dto.Wallet;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    public Wallet getWallet(Long id) {

        return new Wallet(
                id,
                "Raj",
                2500.00
        );

    }

}