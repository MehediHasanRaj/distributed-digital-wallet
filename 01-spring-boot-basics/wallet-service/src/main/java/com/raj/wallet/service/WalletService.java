package com.raj.wallet.service;

import org.springframework.stereotype.Service;

@Service
public class WalletService {

    public String getApplicationStatus() {

        return "Wallet Service is running successfully!";
    }
}