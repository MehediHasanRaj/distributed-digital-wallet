package com.raj.wallet.dto.request;

public class CreateWalletRequest {

    private String owner;

    private Double balance;

    public CreateWalletRequest() {
    }

    public CreateWalletRequest(String owner, Double balance) {
        this.owner = owner;
        this.balance = balance;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

}