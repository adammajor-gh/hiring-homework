package com.adam.hiring.account;

import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);


    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    public AccountDto createAccount(AccountDto accountDto) {
        Account account = accountMapper.toEntity(accountDto);
        Account savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }

    public List<AccountDto> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<AccountDto> getAccountById(Long id) {
        return accountRepository.findById(id)
                .map(accountMapper::toDto);
    }

    public AccountDto updateAccount(Long id, AccountDto accountDetails) {
        return accountRepository.findById(id).map(account -> {
            account.setName(accountDetails.name());
            account.setBalance(accountDetails.balance());
            account.setCurrency(accountDetails.currency());
            Account updatedAccount = accountRepository.save(account);
            return accountMapper.toDto(updatedAccount);
        }).orElseThrow(() -> new RuntimeException("Account not found with id " + id));
    }

    public Account deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
        accountRepository.delete(account);
        return account;
    }

    @Transactional
    public AccountDto withdraw(Long accountId, BigDecimal amount) {
        logger.debug("Withdrawing {} from account: {}", amount, accountId);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        if (account.getBalance().compareTo(amount) < 0) {
            logger.warn("Insufficient funds. Account: {}, Attempted: {}, Balance: {}", accountId, amount, account.getBalance());
            throw new IllegalStateException("Insufficient funds, balance: " + account.getBalance());
        }

        account.setBalance(account.getBalance().subtract(amount));
        Account updatedAccount = accountRepository.save(account);

        logger.info("Withdraw from account: {}, amount: {} success.", accountId, amount);


        return accountMapper.toDto(updatedAccount);
    }

    @Transactional
    public AccountDto deposit(Long accountId, BigDecimal amount) {
        logger.debug("Depositing {} to account: {}", amount, accountId);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        account.setBalance(account.getBalance().add(amount));
        Account updatedAccount = accountRepository.save(account);

        logger.info("Deposit to account: {}, amount: {} success.", accountId, amount);

        return accountMapper.toDto(updatedAccount);
    }
}