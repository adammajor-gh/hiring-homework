package com.adam.hiring.account;

import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public Account toEntity(AccountDto dto) {
        if (dto == null) {
            return null;
        }
        Account account = new Account();
        account.setId(dto.id());
        account.setName(dto.name());
        account.setBalance(dto.balance());
        account.setCurrency(dto.currency());
        return account;
    }

    public AccountDto toDto(Account entity) {
        if (entity == null) {
            return null;
        }
        return new AccountDto(
                entity.getId(),
                entity.getName(),
                entity.getBalance(),
                entity.getCurrency()
        );
    }
}
