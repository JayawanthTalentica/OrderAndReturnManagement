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
* All transitions are atomically committed
* All transitions are logged in append-only audit tables
```
**Flow Description:**
- Orders start in PENDING_PAYMENT, move forward only via allowed actions.
- Terminal states (DELIVERED, CANCELLED) block further transitions.
- Every transition is logged for traceability.

### 1.2 Return Workflow State Machine

```
[REQUESTED] --APPROVE--> [APPROVED] --MARK_IN_TRANSIT--> [IN_TRANSIT] --RECEIVE--> [RECEIVED] --COMPLETE--> [COMPLETED]
   |\
   | \--REJECT--> [REJECTED]

* REJECTED and COMPLETED are terminal states
* Only one return per order
* All transitions are atomically committed
* All transitions are logged in append-only audit tables
```
**Flow Description:**
- Returns can only be created for DELIVERED orders.
- Terminal states (REJECTED, COMPLETED) block further transitions.
- Every transition is logged for traceability.

---

## 2. Database Schema & Transaction Boundaries
- All state transitions are committed in atomic transactions.
- Audit tables are append-only; no updates or deletes.
- History tables provide full traceability for compliance and debugging.

---

## 3. Implementation Notes

- State transitions are enforced in the service layer using dedicated state machine classes.
- All transitions are validated before persisting changes.
- Audit logs (state history) are written atomically with state changes.
- Background jobs (invoice generation, refund processing) are triggered on specific state transitions but are not part of the main transaction.

---

For further details, see `docs/TECHNICAL_DESIGN.md` and `docs/PROJECT_STRUCTURE.md`.
