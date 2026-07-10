package com.raj.wallet.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WithdrawRequest(

        @NotNull

        @DecimalMin(value="0.01")

        BigDecimal amount

) {
}