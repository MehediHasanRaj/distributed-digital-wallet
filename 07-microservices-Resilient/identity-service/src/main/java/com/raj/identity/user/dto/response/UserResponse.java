package com.raj.identity.user.dto.response;

import com.raj.identity.user.enums.UserStatus;

import java.util.UUID;

public record UserResponse(

        UUID userId,

        String firstName,

        String lastName,

        String email,

        UserStatus status

) {
}