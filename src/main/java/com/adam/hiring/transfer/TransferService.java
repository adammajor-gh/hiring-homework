package com.adam.hiring.transfer;

import com.adam.hiring.account.AccountService;
import com.adam.hiring.exchangerate.ExchangeRateResponse;
import com.adam.hiring.exchangerate.ExchangeRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;
    private final ExchangeRateService exchangeRateService;
    private final AccountService accountService;
    private final ApplicationEventPublisher eventPublisher;

    public TransferService(TransferRepository transferRepository,
                           TransferMapper transferMapper,
                           ExchangeRateService exchangeRateService,
                           AccountService accountService,
                           ApplicationEventPublisher eventPublisher) {
        this.transferRepository = transferRepository;
        this.transferMapper = transferMapper;
        this.exchangeRateService = exchangeRateService;
        this.accountService = accountService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TransferDto transfer(TransferDto transferDto) {
        Transfer transfer = transferMapper.toEntity(transferDto);

        Long sourceAccountId = transfer.getSourceAccount().getId();
        Long destinationAccountId = transfer.getDestinationAccount().getId();

        if (sourceAccountId.equals(destinationAccountId)) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same.");
        }

        BigDecimal exchangeRate = BigDecimal.valueOf(1);
        BigDecimal convertedAmount = transfer.getAmount();

        if (!(transfer.getSourceAccount().getCurrency().equals(transfer.getDestinationAccount().getCurrency()))) {
            ExchangeRateResponse exchangeRates = exchangeRateService.getExchangeRates(transfer.getDestinationAccount().getCurrency());
            exchangeRate = exchangeRates.rates().get(transfer.getSourceAccount().getCurrency());
            convertedAmount = transfer.getAmount()
                    .multiply(exchangeRate)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        transfer.setExchangeRate(exchangeRate);
        transfer.setConvertedAmount(convertedAmount);

        accountService.withdraw(sourceAccountId, transfer.getAmount());
        accountService.deposit(destinationAccountId, convertedAmount);

        transfer = transferRepository.save(transfer);

        TransferDto resultDto = new TransferDto(
                transfer.getSourceAccount().getId(),
                transfer.getDestinationAccount().getId(),
                transfer.getAmount(),
                true
        );

        eventPublisher.publishEvent(new TransferCompletedEvent(resultDto));
        return resultDto;
    }
}