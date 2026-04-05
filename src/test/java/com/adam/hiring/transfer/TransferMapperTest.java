package com.adam.hiring.transfer;

import com.adam.hiring.account.Account;
import com.adam.hiring.account.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferMapperTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransferMapper transferMapper;

    private Account sourceAccount;
    private Account destinationAccount;

    @BeforeEach
    void setUp() {
        sourceAccount = new Account();
        sourceAccount.setId(1L);

        destinationAccount = new Account();
        destinationAccount.setId(2L);
    }

    @Test
    void toEntity_ValidDto_ReturnsTransferEntity() {
        TransferDto dto = new TransferDto(1L, 2L, BigDecimal.valueOf(100.50), null);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destinationAccount));

        Transfer transfer = transferMapper.toEntity(dto);

        assertNotNull(transfer);
        assertEquals(1L, transfer.getSourceAccount().getId());
        assertEquals(2L, transfer.getDestinationAccount().getId());
        assertEquals(BigDecimal.valueOf(100.50), transfer.getAmount());
        assertNotNull(transfer.getCreatedAt()); // Verifies the Instant.now() was set
    }

    @Test
    void toEntity_SourceAccountNotFound_ThrowsException() {
        TransferDto dto = new TransferDto(99L, 2L, BigDecimal.TEN, null);

        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            transferMapper.toEntity(dto);
        });

        assertEquals("Source account not found with ID: 99", exception.getMessage());
    }

    @Test
    void toEntity_NullDto_ReturnsNull() {
        Transfer transfer = transferMapper.toEntity(null);
        assertNull(transfer);
    }
}