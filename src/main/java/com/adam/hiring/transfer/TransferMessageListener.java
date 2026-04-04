package com.adam.hiring.transfer;

import com.adam.hiring.artemis.ArtemisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TransferMessageListener {

    private final ArtemisService artemisService;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(TransferMessageListener.class);
    private static final String TRANSFER_QUEUE = "transfer.completed.queue";

    public TransferMessageListener(ArtemisService artemisService, ObjectMapper objectMapper) {
        this.artemisService = artemisService;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransferCompleted(TransferCompletedEvent event) {
        logger.debug("TrasnferCompletedEvent received");
        try {
            String jsonMessage = objectMapper.writeValueAsString(event.transferDto());

            artemisService.send(TRANSFER_QUEUE, jsonMessage);

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize transfer DTO for JMS: {}", e.getMessage());
        }
    }
}