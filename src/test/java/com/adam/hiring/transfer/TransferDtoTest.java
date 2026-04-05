package com.adam.hiring.transfer;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validDto_HasNoViolations() {
        TransferDto dto = new TransferDto(1L, 2L, new BigDecimal("100.00"), null);

        Set<ConstraintViolation<TransferDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "Expected no validation errors");
    }

    @Test
    void nullAmount_TriggersNotNullViolation() {
        TransferDto dto = new TransferDto(1L, 2L, null, null);

        Set<ConstraintViolation<TransferDto>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        ConstraintViolation<TransferDto> violation = violations.iterator().next();
        assertEquals("Balance cannot be null", violation.getMessage());
    }

    @Test
    void amountWithTooManyFractionalDigits_TriggersDigitsViolation() {
        TransferDto dto = new TransferDto(1L, 2L, new BigDecimal("100.123"), null);

        Set<ConstraintViolation<TransferDto>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        ConstraintViolation<TransferDto> violation = violations.iterator().next();
        assertEquals("Balance must have at most 12 integer digits and 2 fractional digits", violation.getMessage());
    }
}