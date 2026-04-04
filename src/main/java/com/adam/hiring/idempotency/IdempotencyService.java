package com.adam.hiring.idempotency;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class IdempotencyService {

    private final IdempotencyRepository repository;

    public IdempotencyService(IdempotencyRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Idempotency> checkOrInitiate(String key, String apiPath) {
        Optional<Idempotency> existingOpt = repository.findById(key);

        if (existingOpt.isPresent()) {
            Idempotency existing = existingOpt.get();

            if (existing.getStatus() == IdempotencyStatus.PROCESSING) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A request with this idempotency key is already processing."
                );
            }

            if (existing.getStatus() == IdempotencyStatus.COMPLETED) {
                return Optional.of(existing);
            }

            if (existing.getStatus() == IdempotencyStatus.FAILED) {
                existing.setStatus(IdempotencyStatus.PROCESSING);
                repository.save(existing);
                return Optional.empty();
            }
        }

        try {
            Idempotency newRecord = new Idempotency();
            newRecord.setIdempotencyKey(key);
            newRecord.setApiPath(apiPath);
            newRecord.setStatus(IdempotencyStatus.PROCESSING);
            repository.saveAndFlush(newRecord);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A request with this idempotency key is already processing."
            );
        }

        return Optional.empty();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(String key, String payload, int statusCode) {
        repository.findById(key).ifPresent(record -> {
            record.setStatus(IdempotencyStatus.COMPLETED);
            record.setResponsePayload(payload);
            record.setHttpStatusCode(statusCode);
            repository.save(record);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(String key) {
        repository.findById(key).ifPresent(record -> {
            record.setStatus(IdempotencyStatus.FAILED);
            repository.save(record);
        });
    }
}