# Order & Returns Management System

## Project Overview

A robust backend system for managing customer orders and returns, designed for the ArtiCurated marketplace. Implements strict state machines, async background jobs, audit logging, and operational transparency.

## Business Problem Solved

- Ensures reliable order and return lifecycle management.
- Prevents invalid transitions and duplicate jobs.
- Provides full audit and job execution visibility for compliance and debugging.

## Key Features

- Strict state machine enforcement for orders and returns
- Audit history APIs for all transitions
- Background job visibility via JobExecution APIs
- Asynchronous execution using ThreadPoolTaskExecutor
- Idempotent job handling and retry strategy
- Health checks and operational transparency
- Dockerized deployment with PostgreSQL
- Local development with H2
- Comprehensive unit and integration tests
- JaCoCo coverage reporting

## State Machine Enforcement

- All transitions validated by dedicated state machine classes
- Terminal states block further transitions
- Invalid transitions throw exceptions

## Audit Visibility APIs

- `/api/orders/{orderId}/history` and `/api/returns/{returnId}/history` return full transition history
- 404 for non-existent entities
- Paginated responses with total count

## Background Job Visibility APIs

- `/api/jobs/{jobId}`: Get job execution by ID
- `/api/jobs?entityId=`: Get jobs for order/return
- `/api/orders/{orderId}/jobs`, `/api/returns/{returnId}/jobs`: Entity-specific job tracking
- JobExecutionResponse includes status, attempts, errors, timestamps

## Async Execution Model

- Invoice and refund jobs run via Spring's @Async and ThreadPoolTaskExecutor
- Configured with corePoolSize=5, maxPoolSize=20, queueCapacity=100
- Jobs are tracked and retried up to 3 times
- Idempotency: No duplicate RUNNING/SUCCESS jobs for same entity/type

## Retry Strategy

- Jobs retry up to 3 times on failure
- Status and error details updated on each attempt
- Final state is SUCCESS or FAILED

## Database Setup

- PostgreSQL for Docker profile
- H2 for local/dev profile
- All tables indexed for efficient queries

## Docker Setup

- `docker-compose.yml` runs app and DB as separate containers
- Health checks for DB (`pg_isready`) and app (`/actuator/health`)
- App waits for DB readiness before starting
- Invoices directory mapped as volume

## Running Locally

```sh
mvn spring-boot:run
```

- API: [http://localhost:8080](http://localhost:8080)
- H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

## Running with Docker

```sh
docker compose up --build
```

- API: [http://localhost:8080](http://localhost:8080)

## Health Checks

- `/actuator/health` and `/actuator/info` exposed via Spring Boot Actuator
- Docker Compose healthchecks ensure robust startup and monitoring

## Testing Endpoints

- Use Swagger Editor or Postman with `API-SPECIFICATION.yml`
- All endpoints covered by integration tests

## Generating Coverage Report

```sh
mvn clean verify
```

- Coverage report: `orderandreturnmanagement/target/site/jacoco/index.html`
- Minimum expected coverage: 70% overall

## Actuator Endpoints

- `/actuator/health`: Health status
- `/actuator/info`: App info

## Async Job Architecture

- All async jobs run via ThreadPoolTaskExecutor
- Single-service async model, suitable for assignment scope

## System Completeness
- **End-to-End Execution:**
  - Runs via `docker compose up --build`.
  - Includes PostgreSQL container, application container, async job execution, JobExecution tracking, health checks via Actuator, and mounted invoice volume.
  - No external dependencies required.
- **Health Checks:**
  - `/actuator/health` and `/actuator/info` endpoints for operational transparency.
  - Docker Compose healthchecks ensure readiness and monitoring.

---

## Operational Transparency
- **Audit History APIs:** Provide full traceability of all order and return transitions.
- **JobExecution APIs:** Expose async job status, attempts, errors, and timestamps.
- **Actuator Endpoints:** `/actuator/health` and `/actuator/info` for system health and info.
- **Logs:** All state transitions, job executions, retries, and failures are logged to console and container logs.

---

## Error Handling Strategy
- **400 Bad Request:** Invalid transitions, validation errors.
- **404 Not Found:** Entity not found (order, return, job).
- **409 Conflict:** Attempted transition from terminal state, duplicate return.
- **500 Internal Server Error:** Unexpected errors.
- **Consistency:** All error responses use structured ErrorResponse DTOs for clarity and debugging.

---

## Coverage Reporting
- **How to Run:**
  ```sh
  mvn clean verify
  ```
- **Report Location:**
  `orderandreturnmanagement/target/site/jacoco/index.html`
- **Coverage Metrics:**
  - Overall Coverage: XX% (see report)
  - State Machine Coverage: ~100% branch coverage

---

## Notes

- This is a single-service async architecture for assignment scope
- All business rules, audit, and job visibility are implemented
- See ARCHITECTURE.md and WORKFLOW_DESIGN.md for diagrams and deeper explanations
