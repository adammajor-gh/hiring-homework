package com.adam.hiring.transfer;

import com.adam.hiring.idempotency.Idempotency;
import com.adam.hiring.idempotency.IdempotencyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public TransferController(TransferService transferService,
                              IdempotencyService idempotencyService,
                              ObjectMapper objectMapper) {
        this.transferService = transferService;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<?> transfer(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferDto transferDto) throws JsonProcessingException {

        Optional<Idempotency> cachedResponse = idempotencyService.checkOrInitiate(idempotencyKey, "/api/transfers");

        if (cachedResponse.isPresent()) {
            Idempotency idempotency = cachedResponse.get();
            TransferDto savedDto = objectMapper.readValue(idempotency.getResponsePayload(), TransferDto.class);
            return ResponseEntity.status(idempotency.getHttpStatusCode()).body(savedDto);
        }

        try {
            TransferDto result = transferService.transfer(transferDto);
            String payload = objectMapper.writeValueAsString(result);
            idempotencyService.complete(idempotencyKey, payload, HttpStatus.CREATED.value());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            idempotencyService.fail(idempotencyKey);
            throw e;
        }
    }
}