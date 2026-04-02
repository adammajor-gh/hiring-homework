package com.adam.hiring.account;

import com.adam.hiring.shared.enums.Currency;

import java.math.BigDecimal;

public record AccountDto(
        Long id,
        String name,
        BigDecimal balance,
        Currency currency
) { }
