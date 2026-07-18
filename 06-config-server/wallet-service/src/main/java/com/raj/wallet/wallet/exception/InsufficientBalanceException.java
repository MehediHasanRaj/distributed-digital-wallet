package com.raj.wallet.wallet.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException() {
        super("Insufficient wallet balance.");
    }

}