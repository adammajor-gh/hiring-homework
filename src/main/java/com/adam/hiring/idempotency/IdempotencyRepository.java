package com.adam.hiring.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyRepository extends JpaRepository<Idempotency, String> {
}