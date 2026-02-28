package com.orderreturn.service.audit;

import com.orderreturn.dto.PageResponse;
import com.orderreturn.dto.OrderStateHistoryResponse;
import com.orderreturn.entities.Order;
import com.orderreturn.entities.OrderStateHistory;
import com.orderreturn.enums.ActorType;
import com.orderreturn.enums.OrderState;
import com.orderreturn.exception.NotFoundException;
import com.orderreturn.repositories.OrderRepository;
import com.orderreturn.repositories.OrderStateHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderStateAuditService {
    private final OrderStateHistoryRepository historyRepository;
    private final OrderRepository orderRepository;

    public OrderStateAuditService(OrderStateHistoryRepository historyRepository, OrderRepository orderRepository) {
        this.historyRepository = historyRepository;
        this.orderRepository = orderRepository;
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

    public PageResponse<OrderStateHistoryResponse> getOrderHistory(UUID orderId, int page, int size) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").ascending());
        Page<OrderStateHistory> historyPage = historyRepository.findByOrderId(orderId, pageable);
        List<OrderStateHistoryResponse> content = historyPage.getContent().stream()
                .map(h -> new OrderStateHistoryResponse(
                        h.getId(),
                        h.getOrderId(),
                        h.getPreviousState(),
                        h.getNewState(),
                        h.getActorType(),
                        h.getTimestamp()
                ))
                .collect(Collectors.toList());
        return new PageResponse<>(content, historyPage.getTotalElements(), historyPage.getTotalPages());
    }
}
