package com.orderreturn.service;

import com.orderreturn.entities.Order;
import com.orderreturn.enums.ActorType;
import com.orderreturn.enums.OrderAction;
import com.orderreturn.enums.OrderState;
import com.orderreturn.repositories.OrderRepository;
import com.orderreturn.service.audit.OrderStateAuditService;
import com.orderreturn.service.job.InvoiceGenerationJob;
import com.orderreturn.service.state.OrderStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderStateAuditService auditService;
    private final InvoiceGenerationJob invoiceGenerationJob;

    public OrderService(OrderRepository orderRepository, OrderStateAuditService auditService, InvoiceGenerationJob invoiceGenerationJob) {
        this.orderRepository = orderRepository;
        this.auditService = auditService;
        this.invoiceGenerationJob = invoiceGenerationJob;
    }

    public Order createOrder() {
        Order order = Order.builder().build(); // state defaults to PENDING_PAYMENT
        return orderRepository.save(order);
    }

    @Transactional
    public Order transitionOrder(UUID orderId, OrderAction action, ActorType actorType) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        OrderState currentState = order.getState();
        if (isTerminal(currentState)) {
            throw new IllegalStateException("Cannot transition from terminal state: " + currentState);
        }
        OrderState newState = OrderStateMachine.transition(currentState, action);
        order.setState(newState);
        orderRepository.save(order);
        auditService.auditOrderStateTransition(orderId, currentState, newState, actorType);
        if (newState == OrderState.SHIPPED) {
            triggerInvoiceGenerationJob(orderId);
        }
        return order;
    }

    private boolean isTerminal(OrderState state) {
        return state == OrderState.CANCELLED || state == OrderState.DELIVERED;
    }

    // Placeholder for background job trigger (implementation in later task)
    private void triggerInvoiceGenerationJob(UUID orderId) {
        invoiceGenerationJob.generateInvoiceAsync(orderId);
    }

    public Order getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (order.getState() == OrderState.CANCELLED) {
            throw new IllegalArgumentException("Order is CANCELLED: " + orderId);
        }
        return order;
    }
}
