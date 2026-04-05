Hiring Homework

## Architectural choices and design decisions

### Overview

The solution consists of two runtime components:

1. The main application: Java Spring Boot REST service
2. Apache ActiveMQ Artemis: message broker for event propagation

### Main application
The application is built on Java 25 and Spring Boot 4.0.5, initialised via Spring Initializr (`https://start.spring.io/`).

- Java 25 was chosen as the most recent LTS (Long Term Support) release.
- Spring Boot 4.0.5 is the latest stable Spring Boot release.
- The database is H2 in-memory, matching the requirement.
- Swagger UI available at: `/swagger-ui/index.html`.
- H2 console available at: `/h2-console`

### Apache ActiveMQ Artemis
Artemis is used to propagate successful transfer events to external domain services (e.g. Fraud Detection, Notification Center).

I chose Artemis, because
1. Message persistence: messages survive broker restarts, so a downstream service going offline does not result in lost events.
2. Familiarity and relative ease of operation compared to alternatives like Kafka for this scale of problem.

## Edge case handling

### Resilience — flaky external Exchange Rate API

The exchange rate API is simulated as flaky using a Wiremock server. It uses a scenario-based state machine per currency to randomly return 503 Service Unavailable with randomised latency (500–5000 ms), with a guaranteed success on the 5th attempt.
To handle this, i used Resilience4j's Retry mechanism. The retry policy is configured in application.yaml:
Only 503 errors trigger a retry. 4xx client errors are not retried, because those indicate a bad request that would fail on every attempt regardless.

### Concurrency — protecting account balances

Account balances are modified by two concurrent operations: withdraw (source account) and deposit (destination account). Without coordination, two simultaneous transfers involving the same account could both read the same stale balance and produce an incorrect final value (lost update problem). This is solved with pessimistic write locking in the AccountRepository

When withdraw or deposit calls findByIdForUpdate, the database issues a SELECT ... FOR UPDATE, which blocks any other transaction from reading or writing that row until the current transaction completes. This serialises concurrent access to the same account and guarantees balance correctness.

Both withdraw and deposit are annotated with @Transactional, and the transfer method is is also @Transactional, meaning the entire transfer (withdraw + deposit + save) is one atomic unit — it either fully succeeds or fully rolls back.

### Reliability — guaranteed-once event delivery

The @TransactionalEventListener(phase = AFTER_COMMIT) in the TransferMessageListener Java class ensures that the Artemis message is only dispatched after the database transaction commits successfully. This means:
Artemis's message persistence (durable queue) ensures that even if a downstream consumer is temporarily unavailable, the message is not lost and will be delivered once it reconnects.

## Build and run

### Prerequisites

- Java 25+
- Maven 3.9+
- Docker & Docker Compose

### 1. Start the Artemis broker

The application requires a running Artemis instance. Start it with the provided Docker Compose file:

```
cd docker
docker compose up -d
```

This starts Artemis with:
- Broker: `localhost:61616`
- GUI: `http://localhost:8161`

### 2. Build the application

```
./mvnw clean package
```

### 3. Run the application

```
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080` by default.

The embedded WireMock exchange rate stub starts automatically on port `8081`.

### 4. Run the tests
I just wrote a few example unit and integration tests. My goal was not the 100% coverage, but show the way how am i write test cases.
```
./mvnw test
```