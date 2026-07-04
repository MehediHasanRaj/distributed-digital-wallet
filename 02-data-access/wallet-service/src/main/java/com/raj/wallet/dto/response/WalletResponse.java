package com.raj.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class WalletResponse {

    private Long id;
    private String owner;
    private BigDecimal balance;

}