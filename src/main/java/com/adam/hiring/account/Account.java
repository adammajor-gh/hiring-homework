package com.adam.hiring.account;

import com.adam.hiring.shared.enums.Currency;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name="account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Account name cannot be blank")
    @Size(min = 2, max = 100, message = "Account name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Balance cannot be null")
    @Column(nullable = false, precision = 14, scale = 2)
    //TODO: Negative balance is allowed? If not, we should use @PositiveOrZero annotation too
    private BigDecimal balance;

    @NotNull(message = "Currency cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;
}
