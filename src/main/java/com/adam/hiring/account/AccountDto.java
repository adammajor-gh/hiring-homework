package com.adam.hiring.account;

import com.adam.hiring.shared.enums.Currency;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountDto(
        Long id,

        @NotBlank(message = "Account name cannot be blank")
        String name,

        @NotNull(message = "Balance cannot be null")
        @Digits(integer = 12, fraction = 2, message = "Balance must have at most 12 integer digits and 2 fractional digits")
        //TODO: Negative balance is allowed? If not, we should use @PositiveOrZero annotation too
        BigDecimal balance,

        @NotNull(message = "Currency cannot be null")
        Currency currency
) { }
