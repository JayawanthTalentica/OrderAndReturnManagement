package com.orderreturn.service.audit;

import com.orderreturn.entities.OrderStateHistory;
import com.orderreturn.enums.ActorType;
import com.orderreturn.enums.OrderState;
import com.orderreturn.repositories.OrderStateHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderStateAuditService {
    private final OrderStateHistoryRepository historyRepository;

    public OrderStateAuditService(OrderStateHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Transactional
    public void auditOrderStateTransition(UUID orderId, OrderState previousState, OrderState newState, ActorType actorType) {
        OrderStateHistory history = OrderStateHistory.builder()
                .orderId(orderId)
                .previousState(previousState)
                .newState(newState)
                .actorType(actorType)
                .build();
        historyRepository.save(history);
    }
}

