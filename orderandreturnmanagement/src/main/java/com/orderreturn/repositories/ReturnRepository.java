package com.orderreturn.repositories;

import com.orderreturn.entities.Return;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReturnRepository extends JpaRepository<Return, UUID> {
    Optional<Return> findById(UUID id);
    Optional<Return> findByOrderId(UUID orderId);
    // Enforces one return per order by lookup on orderId
}

