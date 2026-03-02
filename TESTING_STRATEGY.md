# Testing Strategy

## 1. Unit Tests
- **State Machine Coverage:**
  - All valid and invalid transitions for orders and returns
  - Terminal state enforcement
  - Near 100% branch coverage
- **Service Layer:**
  - Business logic, audit logging, job triggering, error handling
- **Idempotency Testing:**
  - JobExecutionService prevents duplicate jobs
  - Safe retry logic
- **Async Job Tests:**
  - Success, failure, and retry paths for invoice and refund jobs
  - Mockito used for mocking repositories and job services

## 2. Integration Tests
- **MockMvc:**
  - Controller endpoint testing
  - Audit history and job execution APIs
- **@SpringBootTest:**
  - H2 in-memory DB for full stack coverage
  - Transactional rollback after each test for isolation

## 3. Transactional & Mocking Strategy
- **Transactional Rollback:**
  - Ensures DB is reset after each test, preventing side effects
- **Mocking for Async Jobs:**
  - Ensures deterministic test outcomes and isolates business logic
- **State Machine Tests:**
  - Critical for enforcing business rules and preventing regression

## 4. Coverage Reporting
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

This strategy ensures all critical business flows, error handling, and async reliability are validated to production standards.
