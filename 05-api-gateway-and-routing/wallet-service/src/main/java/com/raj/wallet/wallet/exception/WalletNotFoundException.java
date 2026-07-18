package com.raj.wallet.wallet.exception;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(String walletId) {
        super("Wallet not found: " + walletId);
    }

}