package com.raj.wallet.wallet.dto.response;

import com.raj.wallet.wallet.enums.WalletStatus;
import com.raj.wallet.wallet.enums.Currency;
import java.math.BigDecimal;

import java.util.UUID;

public record WalletResponse(

        Long id,

        UUID userId,

        BigDecimal balance,

        Currency currency,

        WalletStatus status

) {
}