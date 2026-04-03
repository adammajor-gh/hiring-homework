package com.adam.hiring.transfer;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferDto(
        @NotNull(message = "Source account cannot be null")
        Long sourceAccountId,

        @NotNull(message = "Destination account cannot be null")
        Long destinationAccountId,

        @NotNull(message = "Balance cannot be null")
        @Digits(integer = 12, fraction = 2, message = "Balance must have at most 12 integer digits and 2 fractional digits")
        BigDecimal amount,

        Boolean isSuccess
) {
}
