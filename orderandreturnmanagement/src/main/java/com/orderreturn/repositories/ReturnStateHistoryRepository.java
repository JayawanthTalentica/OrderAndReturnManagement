package com.orderreturn.repositories;

import com.orderreturn.entities.ReturnStateHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReturnStateHistoryRepository extends JpaRepository<ReturnStateHistory, UUID> {
    List<ReturnStateHistory> findByReturnIdOrderByTimestampAsc(UUID returnId);
    // Read-only intent: no delete methods exposed
}

