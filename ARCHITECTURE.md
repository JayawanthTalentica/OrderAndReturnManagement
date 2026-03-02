# Architecture Overview

## System Completeness
- **End-to-End Execution:** System runs fully via `docker compose up --build`.
- **Database + App + Health Checks:** PostgreSQL and app containers, healthchecks ensure readiness and monitoring.
- **Async Execution Included:** Invoice and refund jobs run asynchronously via ThreadPoolTaskExecutor.
- **Observability:** Spring Boot Actuator exposes `/actuator/health` and `/actuator/info` for operational transparency.

---

## Scalability & Future Evolution
- **ThreadPoolTaskExecutor:** Used for async job execution to keep architecture simple and assignment-focused.
- **JobExecution Abstraction:** Allows migration to distributed queues (Kafka/RabbitMQ) without changing controllers/services.
- **Controller/Service Stability:** API and business logic remain unchanged if async jobs are externalized.
- **Assignment-Appropriate, Scalable:** Current design is robust for single-service, but ready for future distributed evolution.

---

## Async Execution Flow Diagram
```
Controller
   ↓
Service
   ↓
State Machine
   ↓
Trigger Async Job
   ↓
@Async Job Class
   ↓
JobExecutionService
   ↓
JobExecution Table
```

---

## Explicit Idempotency Explanation
- **Prevention:** Duplicate RUNNING/SUCCESS jobs for the same entity/type are prevented by JobExecutionService.
- **Importance:** Idempotency ensures safe retries and prevents duplicate processing.
- **Retry Handling:** Retries are tracked via attempts; only one job per entity/type is active at a time.

---

## Async Architecture Trade-off Explanation
- **ThreadPoolTaskExecutor:** Used for async job execution to keep architecture simple and assignment-focused.
- **No Separate Worker Container:** All async jobs run in the main app container for simplicity and reliability.
- **Future Scalability:** JobExecution abstraction supports migration to distributed queues (Kafka/RabbitMQ) if needed.
- **Migration Ready:** JobExecution entity and APIs are designed to support future externalization of job processing.

---

## High-Level Architecture Diagram
```
+-------------------+      +-------------------+      +-------------------+      +-------------------+
|   Controller      | ---> |    Service        | ---> |  State Machine    | ---> |   Repository      |
+-------------------+      +-------------------+      +-------------------+      +-------------------+
        |                        |                        |                        |
        |                        |                        |                        |
        v                        v                        v                        v
+-------------------+      +-------------------+      +-------------------+      +-------------------+
|  Audit Logging    |      |  Async Job        |      |  JobExecution     |      |  Database         |
+-------------------+      +-------------------+      +-------------------+      +-------------------+
```

---

## Layered Design
- **Controller**: Handles HTTP requests, validates input, returns DTOs.
- **Service**: Implements business logic, orchestrates state transitions, triggers jobs.
- **State Machine**: Strictly enforces valid transitions, prevents illegal/terminal transitions.
- **Repository**: Data access layer, interacts with PostgreSQL/H2.

---

## Async Job Execution Flow
- Background jobs (invoice/refund) are triggered by service layer.
- Jobs run via Spring's `@Async` annotation and a dedicated `ThreadPoolTaskExecutor`.
- JobExecution entity tracks job status, attempts, errors, and timestamps.
- Idempotency: Jobs are only created if no existing RUNNING/SUCCESS job for same entity/type.

---

## Audit Logging Flow
- Every state transition is logged in append-only audit tables.
- Audit APIs expose full history for orders and returns.

---

## JobExecution Tracking Flow
- All background jobs create/update JobExecution records.
- Status, attempts, errors, and timestamps are visible via API.

---

## ThreadPoolTaskExecutor
- Configured with corePoolSize=5, maxPoolSize=20, queueCapacity=100, threadNamePrefix="order-return-async-".
- Ensures async jobs do not block main request threads.

---

## Database Interaction Model
- Uses Spring Data JPA for ORM.
- PostgreSQL in Docker, H2 for local/dev.
- All tables have indexes for efficient queries.

---

## Docker Deployment Architecture
- App and DB run as separate containers.
- Health checks for DB (`pg_isready`) and app (`/actuator/health`).
- App waits for DB readiness before starting.
- Invoices directory mapped as volume.

---

## Health Checks & Observability
- Spring Boot Actuator exposes `/actuator/health` and `/actuator/info`.
- Docker Compose healthchecks ensure robust startup and monitoring.

---

## Design Rationale
- **State machines** are isolated for testability and correctness.
- **Audit tables** are append-only for full traceability.
- **Async jobs** are decoupled from main transaction for reliability and scalability.
- **Idempotency** prevents duplicate job execution and ensures safe retries.

---

## Operational Transparency
- **Audit History:** Inspect via audit APIs for orders and returns.
- **Job Executions:** Inspect via job APIs for status, attempts, errors.
- **Health:** Inspect via `/actuator/health` and `/actuator/info`.
- **Logs:** All transitions and job executions are logged for traceability.

---

