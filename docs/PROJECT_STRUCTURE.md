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
- **Controller → Service → Repository**: Clean separation of concerns.
- **State machines** are isolated in `service.state` for testability and clarity.
- **Audit and background jobs** are modularized for maintainability.
- **No business logic in controllers or repositories.**

