package com.raj.wallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wallet")
public class WalletProperties {

    private String currency;

    private Double maximumTransfer;

    private Double minimumBalance;

    // getters and setters
}