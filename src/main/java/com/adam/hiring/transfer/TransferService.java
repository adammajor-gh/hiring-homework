package com.adam.hiring.transfer;

import com.adam.hiring.exchangerate.ExchangeRateResponse;
import com.adam.hiring.exchangerate.ExchangeRateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;
    private final ExchangeRateService exchangeRateService;

    public TransferService(TransferRepository transferRepository,
                           TransferMapper transferMapper,
                           ExchangeRateService exchangeRateService) {
        this.transferRepository = transferRepository;
        this.transferMapper = transferMapper;
        this.exchangeRateService = exchangeRateService;
    }

    @Transactional
    public TransferDto transfer(TransferDto transferDto) {
        Transfer transfer = transferMapper.toEntity(transferDto);

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

        transfer = transferRepository.save(transfer);

        return new TransferDto(
                transfer.getSourceAccount().getId(),
                transfer.getDestinationAccount().getId(),
                transfer.getAmount(),
                true
        );
    }
}