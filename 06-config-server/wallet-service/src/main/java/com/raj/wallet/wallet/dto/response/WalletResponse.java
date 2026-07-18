package com.raj.wallet.wallet.dto.response;

import com.raj.wallet.wallet.enums.Currency;
import com.raj.wallet.wallet.enums.WalletStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletResponse(

        UUID walletId,

        UUID userId,

        BigDecimal balance,

        Currency currency,

        WalletStatus status

) {
}