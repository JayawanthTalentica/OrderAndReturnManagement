# Order & Returns Management System
## Technical Design Document

---

## 1. High-Level Architecture Overview

- **Spring Boot Backend-Only Application**
- Layered architecture:
  - **API Layer**: REST controllers for order and return management
  - **Service Layer**: Business logic, state machine enforcement, orchestration
  - **Persistence Layer**: JPA entities and repositories for data access
  - **Background Job Layer**: Asynchronous processing for invoice generation and refund handling
- No external integrations (payment, shipping, notifications) beyond simulated invoice/refund APIs
- No authentication/authorization (trusted callers only)

---

## 2. Order State Machine

```
PENDING_PAYMENT
    |\
    | \
    |  \---> CANCELLED
    |
    v
  PAID
    |\
    | \----> CANCELLED (only if not yet processed)
    |
    v
PROCESSING_IN_WAREHOUSE
    |
    v
  SHIPPED
    |
    v
 DELIVERED
```
- **No backward transitions**
- **Cancellation is final**
- **No transitions after DELIVERED**

---

## 3. Return Workflow State Machine

```
REQUESTED
   |\
   | \----> REJECTED (terminal)
   |
   v
APPROVED
   |
   v
IN_TRANSIT
   |
   v
RECEIVED
   |
   v
COMPLETED
```
- **Only one return per order**
- **Return can only be requested if order is DELIVERED**
- **Manual approval/rejection by admin**
- **REJECTED is terminal**

---

## 4. Entity Modeling

### Order
- `id: UUID`
- `state: OrderState`
- `createdAt: Instant`
- `updatedAt: Instant`

### Return
- `id: UUID`
- `orderId: UUID` (FK)
- `state: ReturnState`
- `createdAt: Instant`
- `updatedAt: Instant`
- `refundStatus: RefundStatus` (e.g., PENDING, SUCCESS, FAILED)

### OrderStateHistory
- `id: UUID`
- `orderId: UUID` (FK)
- `previousState: OrderState`
- `newState: OrderState`
- `timestamp: Instant`
- `actorType: ActorType` (USER, ADMIN, SYSTEM)

### ReturnStateHistory
- `id: UUID`
- `returnId: UUID` (FK)
- `previousState: ReturnState`
- `newState: ReturnState`
- `timestamp: Instant`
- `actorType: ActorType` (USER, ADMIN, SYSTEM)

---

## 5. Background Job Design

### Invoice Generation
- Triggered when order transitions to SHIPPED
- Generates (simulates) a PDF invoice (one-time only)
- Runs asynchronously
- Retries up to 3 times on failure
- Marks job as FAILED after 3 unsuccessful attempts

### Refund Processing
- Triggered when return transitions to COMPLETED
- Processes (simulates) full refund asynchronously
- Retries up to 3 times on failure
- If all retries fail, refundStatus is set to FAILED (return remains COMPLETED)

### Retry & Failure Handling
- Automatic retry (up to 3 attempts)
- No alerting or notification system
- Failed jobs require manual intervention (out of scope)

---

## 6. Transaction Boundaries
- **Atomic operations:**
  - Order state transition + OrderStateHistory log
  - Return state transition + ReturnStateHistory log
- **Background jobs** are NOT part of the same transaction as state transitions

---

## 7. Error Handling Approach
- **Invalid state transitions:** HTTP 400 Bad Request + descriptive error
- **Entity not found:** HTTP 404 Not Found
- **Duplicate/conflicting operations:** HTTP 409 Conflict
- **All errors:** Consistent, clear error messages in API responses

---

## 8. API Layer Responsibilities (High-Level)
- Expose REST endpoints for order and return management
- Validate input and current state before applying transitions
- Enforce state machine rules and business logic
- Trigger background jobs as required
- Return appropriate HTTP status codes and error messages
- No authentication/authorization logic

---

## 9. Testing Strategy Overview
- **Unit tests:**
  - State machine logic
  - Service layer business rules
  - Background job retry/failure logic
- **Integration tests:**
  - API endpoints
  - Transaction boundaries
  - End-to-end order and return workflows
- **Test coverage:**
  - All state transitions
  - All error and edge cases

---

*End of Technical Design Document*
 