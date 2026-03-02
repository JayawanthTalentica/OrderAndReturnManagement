# Project Structure

## High-Level Folder Structure

```
OrderAndReturnManagement/
├── src/
│   ├── main/
│   │   ├── java/com/orderreturn/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   │   ├── state/
│   │   │   │   ├── audit/
│   │   │   │   ├── job/
│   │   │   ├── repositories/
│   │   │   ├── entities/
│   │   │   ├── enums/
│   │   │   ├── dto/
│   │   │   ├── mapper/
│   │   │   ├── exception/
│   │   │   └── config/ (if present)
│   └── test/
├── docs/
│   ├── API-SPECIFICATION.yml
│   ├── TECHNICAL_DESIGN.md
│   ├── TASK_BREAKDOWN.md
│   ├── TASK_PROGRESS.md
│   ├── PROJECT_STRUCTURE.md
│   └── CHAT_HISTORY.md
├── Dockerfile
├── docker-compose.yml
├── README.md
└── ...
```

## Package Responsibilities

- **controller/**: Exposes REST API endpoints for orders and returns. Thin layer, delegates to services.
- **service/**: Implements business logic for order and return lifecycles.
  - **state/**: Pure state machine logic for allowed transitions.
  - **audit/**: Handles audit logging of state transitions.
  - **job/**: Asynchronous background jobs (invoice generation, refund processing).
- **repositories/**: Spring Data JPA repositories for data access.
- **entities/**: JPA entities for Order, Return, OrderStateHistory, ReturnStateHistory.
- **enums/**: Enum types for states, actions, refund status, and actor types.
- **dto/**: Data Transfer Objects for API requests and responses.
- **mapper/**: Maps entities to DTOs (one-way, entity → DTO).
- **exception/**: Global exception handling and error response models.
- **config/**: (If present) Application configuration beans.

## Layering & Responsibilities
- **Controller → Service → Repository:** Clean separation of concerns.
- **State machines:** Isolated in `service.state` for testability and clarity.
- **Audit and background jobs:** Modularized for maintainability.
- **No business logic in controllers or repositories.**
- **Async jobs:** Implemented in `service.job` and tracked via JobExecution APIs.
- **Append-only audit tables:** All state transitions are logged for traceability and compliance.
- **Atomic transaction boundaries:** State transitions and audit logs are committed atomically.

---

This structure ensures maintainability, testability, and production-grade clarity for all business flows.
