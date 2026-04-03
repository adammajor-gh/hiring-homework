package com.adam.hiring.transfer;

import com.adam.hiring.account.Account;
import com.adam.hiring.shared.enums.Currency;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transfer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Source account is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @NotNull(message = "Destination account is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_account_id", nullable = false)
    private Account destinationAccount;

    @NotNull(message = "Source currency cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "source_currency", nullable = false)
    private Currency sourceCurrency;

    @NotNull(message = "Destination currency cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "destination_currency", nullable = false)
    private Currency destinationCurrency;

    @NotNull(message = "Amount cannot be null")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Exchange rate cannot be null")
    @Column(name = "exchange_rate", nullable = false, precision = 10, scale = 6)
    private BigDecimal exchangeRate;

    @NotNull(message = "Converted amount cannot be null")
    @Column(name = "converted_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal convertedAmount;

    @NotBlank(message = "Status cannot be blank")
    @Column(nullable = false)
    private String status;

    @NotBlank(message = "Idempotency key cannot be blank")
    @Column(name = "idempotency_key", unique = true, nullable = false, updatable = false)
    private String idempotencyKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}