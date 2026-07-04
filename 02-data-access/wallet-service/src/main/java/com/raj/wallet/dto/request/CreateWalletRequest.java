package com.raj.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
public class CreateWalletRequest {
    @NotBlank(message = "Owner name is required")
    @Size(min = 2, max = 50,
            message = "Owner name must be between 2 and 50 characters")
    private String owner;

    @PositiveOrZero(message = "Balance must be greater than or zero")
    private BigDecimal balance;  //financial use this to avoid precesion error

    public CreateWalletRequest() {
    }


}