package com.adam.hiring.transfer;

import com.adam.hiring.account.AccountService;
import com.adam.hiring.exchangerate.ExchangeRateResponse;
import com.adam.hiring.exchangerate.ExchangeRateService;
import com.adam.hiring.shared.enums.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.Map;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;
    private final ExchangeRateService exchangeRateService;
    private final AccountService accountService;
    private final ApplicationEventPublisher eventPublisher;
    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

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
        logger.debug("Initiating transfer request: {}", transferDto);

        try {
            Transfer transfer = transferMapper.toEntity(transferDto);

            Long sourceAccountId = transfer.getSourceAccount().getId();
            Long destinationAccountId = transfer.getDestinationAccount().getId();
            Currency sourceCurrency = transfer.getSourceAccount().getCurrency();
            Currency destCurrency = transfer.getDestinationAccount().getCurrency();

            if (sourceCurrency == null || destCurrency == null) {
                logger.error("Account currency is missing, source: ({}), destination: ({}).", sourceAccountId, destinationAccountId);
                throw new IllegalStateException("Account currency missing.");
            }

            BigDecimal exchangeRate = BigDecimal.valueOf(1);
            BigDecimal convertedAmount = transfer.getAmount();

            if (!sourceCurrency.equals(destCurrency)) {
                logger.debug("Different currency. Fetching exchange rates for {} to {}.", sourceCurrency, destCurrency);

                ExchangeRateResponse exchangeRates = exchangeRateService.getExchangeRates(sourceCurrency);

                if (exchangeRates == null || exchangeRates.rates() == null) {
                    logger.error("Failed to retrieve exchange rates for currency: {}", sourceCurrency);
                    throw new IllegalStateException("Exchange rate is null.");
                }

                Map<Currency, BigDecimal> rates = exchangeRates.rates();
                if (!rates.containsKey(destCurrency)) {
                    logger.error("Rate not found for destination currency: {}", destCurrency);
                    throw new IllegalStateException("Rate not found for destination currency");
                }

                exchangeRate = rates.get(destCurrency);
                convertedAmount = transfer.getAmount()
                        .multiply(exchangeRate)
                        .setScale(2, RoundingMode.HALF_UP);

                logger.debug("Applied exchange rate: {}. Converted amount: {}", exchangeRate, convertedAmount);
            }

            transfer.setExchangeRate(exchangeRate);
            transfer.setConvertedAmount(convertedAmount);

            accountService.withdraw(sourceAccountId, transfer.getAmount());
            accountService.deposit(destinationAccountId, convertedAmount);

            transfer = transferRepository.save(transfer);
            logger.debug("Transfer entity saved successfully with ID: {}", transfer.getId());

            TransferDto resultDto = new TransferDto(
                    transfer.getSourceAccount().getId(),
                    transfer.getDestinationAccount().getId(),
                    transfer.getAmount(),
                    true
            );

            eventPublisher.publishEvent(new TransferCompletedEvent(resultDto));
            logger.info("Transfer completed successfully. Source: {}, Destination: {}, Amount: {}",
                    sourceAccountId, destinationAccountId, transfer.getAmount());

            return resultDto;

        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", transferDto, e);
            throw e;
        }
    }
}