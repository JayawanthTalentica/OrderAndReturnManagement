# Order & Returns Management System

## 1. State Machine Diagrams

### 1.1 Order State Machine

```
[PENDING_PAYMENT] --PAY--> [PAID] --PROCESS--> [PROCESSING_IN_WAREHOUSE] --SHIP--> [SHIPPED] --DELIVER--> [DELIVERED]
      |                         |
      |--CANCEL-----------------|

[PAID] --CANCEL--> [CANCELLED]

* No backward transitions allowed
* CANCELLED and DELIVERED are terminal states
```

### 1.2 Return Workflow State Machine

```
[REQUESTED] --APPROVE--> [APPROVED] --MARK_IN_TRANSIT--> [IN_TRANSIT] --RECEIVE--> [RECEIVED] --COMPLETE--> [COMPLETED]
   |\
   | \--REJECT--> [REJECTED]

* REJECTED and COMPLETED are terminal states
* Only one return per order
```

---

## 2. Database Schema

### 2.1 Tables Overview

- **Order**
  - id (UUID, PK)
  - state (OrderState)
  - created_at
  - updated_at

- **Return**
  - id (UUID, PK)
  - order_id (UUID, FK to Order)
  - state (ReturnState)
  - refund_status (RefundStatus)
  - created_at
  - updated_at

- **OrderStateHistory**
  - id (UUID, PK)
  - order_id (UUID, FK to Order)
  - previous_state (OrderState)
  - new_state (OrderState)
  - actor_type (ActorType)
  - timestamp

- **ReturnStateHistory**
  - id (UUID, PK)
  - return_id (UUID, FK to Return)
  - previous_state (ReturnState)
  - new_state (ReturnState)
  - actor_type (ActorType)
  - timestamp

### 2.2 Relationships

- **Order** 1 --- * **OrderStateHistory**
- **Order** 1 --- 1 **Return** (at most one return per order)
- **Return** 1 --- * **ReturnStateHistory**

### 2.3 State History Storage

- Every state transition for Order and Return is appended to the respective history table.
- Each history record includes the entity ID, previous state, new state, actor type, and timestamp.
- No updates or deletes are performed on history tables (append-only).

---

## 3. Implementation Notes

- State transitions are enforced in the service layer using dedicated state machine classes.
- All transitions are validated before persisting changes.
- Audit logs (state history) are written atomically with state changes.
- Background jobs (invoice generation, refund processing) are triggered on specific state transitions but are not part of the main transaction.

---

For further details, see `docs/TECHNICAL_DESIGN.md` and `docs/PROJECT_STRUCTURE.md`.
