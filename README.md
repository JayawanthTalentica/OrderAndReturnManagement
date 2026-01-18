# Order & Returns Management System

## Project Overview

The **Order & Returns Management System** is a backend service for the **ArtiCurated** marketplace.  
It manages the complete lifecycle of customer orders and returns using strict state machines, asynchronous background jobs, and comprehensive audit logging.

The system is designed to be robust, maintainable, and evaluator-friendly, strictly following the provided PRD and clarified requirements.

---

## Core Capabilities

- Order lifecycle management (creation → payment → processing → shipment → delivery/cancellation)
- Return workflow with manual approval or rejection
- Strict state machine enforcement for all order and return transitions
- Asynchronous background jobs:
  - Invoice generation on order shipment
  - Refund processing on return completion
- Full audit logging of all state transitions
- Clean separation of concerns across layers

---

## Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL (Docker / Production)
- H2 (Local / Development)
- Docker & Docker Compose
- OpenAPI 3.0 (`API-SPECIFICATION.yml`)
- Maven

---

## Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven (optional, for local development)

---

## Running the Application

### Option 1: Using Docker (Recommended)

Build and start all services using Docker Compose:

```sh
docker-compose up --build
```

Access the application:

- **API Base URL:** [http://localhost:8080](http://localhost:8080)

The application runs using the `docker` Spring profile and connects to a PostgreSQL container.

---

### Option 2: Local Development

Run the application locally using H2 in-memory database:

```sh
mvn spring-boot:run
```

Access:

- **API Base URL:** [http://localhost:8080](http://localhost:8080)
- **H2 Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

---

## API Documentation

- OpenAPI 3.0 specification is available at:
  - `docs/API-SPECIFICATION.yml`
- The specification can be imported into:
  - [Swagger Editor](https://editor.swagger.io/)
  - Postman

---

## Testing

### Unit Tests

**Covers:**
- Order and return state machines
- Service layer business logic
- Background job retry and failure handling

**Run:**
```sh
mvn test
```

### Integration Tests

**Covers:**
- End-to-end API flows using MockMvc
- Controllers, services, repositories, and database (H2)

**Run:**
```sh
mvn verify
```

All tests are expected to pass from a clean setup.

---

## Notes

- Authentication and authorization are intentionally out of scope.
- Payment and shipping integrations are simulated.
- Only one return is allowed per order.
- All state transitions are audited and retained.

---

## License

This project is developed as part of an AI-assisted backend engineering assignment.
