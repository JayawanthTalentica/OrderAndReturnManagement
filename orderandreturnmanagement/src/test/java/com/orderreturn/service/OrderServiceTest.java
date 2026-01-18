package com.orderreturn.service;

import com.orderreturn.entities.Order;
import com.orderreturn.enums.ActorType;
import com.orderreturn.enums.OrderAction;
import com.orderreturn.enums.OrderState;
import com.orderreturn.repositories.OrderRepository;
import com.orderreturn.service.audit.OrderStateAuditService;
import com.orderreturn.service.job.InvoiceGenerationJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository orderRepository;
    @Mock OrderStateAuditService auditService;
    @Mock InvoiceGenerationJob invoiceGenerationJob;
    @InjectMocks OrderService orderService;

    UUID orderId;
    Order order;

    @BeforeEach
    void setup() {
        orderId = UUID.randomUUID();
        order = Order.builder().id(orderId).state(OrderState.PENDING_PAYMENT).build();
    }

    @Test
    void createOrder_setsInitialState() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        Order created = orderService.createOrder();
        assertEquals(OrderState.PENDING_PAYMENT, created.getState());
    }

    @Test
    void transitionOrder_validTransition_updatesStateAndAudits() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        Order transitioned = orderService.transitionOrder(orderId, OrderAction.PAY, ActorType.USER);
        assertEquals(OrderState.PAID, transitioned.getState());
        verify(auditService).auditOrderStateTransition(eq(orderId), eq(OrderState.PENDING_PAYMENT), eq(OrderState.PAID), eq(ActorType.USER));
        verify(invoiceGenerationJob, never()).generateInvoiceAsync(any());
    }

    @Test
    void transitionOrder_toShipped_triggersInvoiceJob() {
        order.setState(OrderState.PROCESSING_IN_WAREHOUSE);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        Order transitioned = orderService.transitionOrder(orderId, OrderAction.SHIP, ActorType.USER);
        assertEquals(OrderState.SHIPPED, transitioned.getState());
        verify(invoiceGenerationJob).generateInvoiceAsync(orderId);
    }

    @Test
    void transitionOrder_fromTerminal_throws() {
        order.setState(OrderState.CANCELLED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        assertThrows(IllegalStateException.class, () -> orderService.transitionOrder(orderId, OrderAction.PAY, ActorType.USER));
    }

    @Test
    void transitionOrder_invalidTransition_throws() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        assertThrows(IllegalStateException.class, () -> orderService.transitionOrder(orderId, OrderAction.PROCESS, ActorType.USER));
    }

    @Test
    void getOrderById_found_returnsOrder() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        Order found = orderService.getOrderById(orderId);
        assertEquals(orderId, found.getId());
    }

    @Test
    void getOrderById_cancelled_throws() {
        order.setState(OrderState.CANCELLED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        assertThrows(IllegalArgumentException.class, () -> orderService.getOrderById(orderId));
    }

    @Test
    void getOrderById_notFound_throws() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> orderService.getOrderById(orderId));
    }
}
