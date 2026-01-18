package com.orderreturn.repositories;

import com.orderreturn.entities.Order;
import com.orderreturn.enums.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByIdAndStateNot(UUID id, OrderState excludedState);
    // Usage: findByIdAndStateNot(id, OrderState.CANCELLED) to exclude cancelled orders
}

