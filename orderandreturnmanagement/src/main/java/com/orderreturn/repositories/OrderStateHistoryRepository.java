package com.orderreturn.repositories;

import com.orderreturn.entities.OrderStateHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderStateHistoryRepository extends JpaRepository<OrderStateHistory, UUID> {
    Page<OrderStateHistory> findByOrderId(UUID orderId, Pageable pageable);
    // Read-only intent: no delete methods exposed
}
