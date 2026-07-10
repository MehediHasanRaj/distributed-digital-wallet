package com.raj.wallet.wallet.dto.request;

import com.raj.wallet.wallet.enums.Currency;
import java.util.UUID;

public record CreateWalletRequest(

        UUID userId,

        Currency currency

) {
}