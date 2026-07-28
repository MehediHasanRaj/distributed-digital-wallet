package com.raj.wallet.identity.dto;

import java.util.UUID;

public record UserSummaryResponse(

        UUID userId,
        String firstName,
        String lastName,
        String email,
        String status

) {
}