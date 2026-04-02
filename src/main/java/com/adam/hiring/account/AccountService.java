package com.adam.hiring.account;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

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
}