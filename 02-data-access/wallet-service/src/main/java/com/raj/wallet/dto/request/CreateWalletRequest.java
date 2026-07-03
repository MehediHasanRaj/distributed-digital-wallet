package com.raj.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateWalletRequest {
    @NotBlank(message = "Owner name is required")
    @Size(min = 2, max = 50,
            message = "Owner name must be between 2 and 50 characters")
    private String owner;

    @PositiveOrZero(message = "Balance must be greater than or zero")
    private double balance;  //financial use this to avoid precesion error

    public CreateWalletRequest() {
    }

    public CreateWalletRequest(String owner, double balance) {
        this.owner = owner;
        this.balance = balance;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

}