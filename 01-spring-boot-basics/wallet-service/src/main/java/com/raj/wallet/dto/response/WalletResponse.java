package com.raj.wallet.dto.response;

public class WalletResponse {

    private Long id;

    private String owner;

    private Double balance;

    public WalletResponse() {
    }

    public WalletResponse(Long id, String owner, Double balance) {
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

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

}