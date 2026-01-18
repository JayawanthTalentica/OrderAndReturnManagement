package com.orderreturn.repositories;

import com.orderreturn.entities.OrderStateHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderStateHistoryRepository extends JpaRepository<OrderStateHistory, UUID> {
    List<OrderStateHistory> findByOrderIdOrderByTimestampAsc(UUID orderId);
    // Read-only intent: no delete methods exposed
}

