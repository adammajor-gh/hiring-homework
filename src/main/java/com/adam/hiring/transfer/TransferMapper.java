package com.adam.hiring.transfer;

import com.adam.hiring.account.Account;
import com.adam.hiring.account.AccountRepository;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityNotFoundException;

import java.time.Instant;

@Component
public class TransferMapper {

    private final AccountRepository accountRepository;

    public TransferMapper(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Transfer toEntity(TransferDto dto) {
        if (dto == null) {
            return null;
        }

        Transfer transfer = new Transfer();

        Account sourceAccount = accountRepository.findById(dto.sourceAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Source account not found with ID: " + dto.sourceAccountId()));

        Account destinationAccount = accountRepository.findById(dto.destinationAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Destination account not found with ID: " + dto.destinationAccountId()));

        transfer.setSourceAccount(sourceAccount);
        transfer.setDestinationAccount(destinationAccount);
        transfer.setAmount(dto.amount());
        transfer.setCreatedAt(Instant.now());

        return transfer;
    }
}