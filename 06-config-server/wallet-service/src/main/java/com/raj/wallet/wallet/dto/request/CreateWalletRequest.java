package com.raj.wallet.wallet.dto.request;

import com.raj.wallet.wallet.enums.Currency;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateWalletRequest(

        @NotNull
        UUID userId,

        @NotNull
        Currency currency

) {
}