package com.raj.wallet.dto.response;

import java.math.BigDecimal;

public class WalletResponse {

    private Long id;

    private String owner;

    private double balance;



    public WalletResponse(Long id, String owner, double balance) {
        this.id = id;
        this.owner = owner;
        this.balance = balance;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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