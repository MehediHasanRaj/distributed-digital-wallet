package com.raj.wallet.wallet.exception;

public class WalletFrozenException extends RuntimeException {

    public WalletFrozenException() {
        super("Wallet is frozen.");
    }

}