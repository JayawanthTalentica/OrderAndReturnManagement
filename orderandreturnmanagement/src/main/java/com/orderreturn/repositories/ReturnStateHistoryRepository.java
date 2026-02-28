package com.orderreturn.repositories;

import com.orderreturn.entities.ReturnStateHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReturnStateHistoryRepository extends JpaRepository<ReturnStateHistory, UUID> {
    Page<ReturnStateHistory> findByReturnId(UUID returnId, Pageable pageable);
    // Read-only intent: no delete methods exposed
}
