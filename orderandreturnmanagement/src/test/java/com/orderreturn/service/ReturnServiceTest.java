package com.orderreturn.service;

import com.orderreturn.entities.Order;
import com.orderreturn.entities.Return;
import com.orderreturn.enums.*;
import com.orderreturn.repositories.OrderRepository;
import com.orderreturn.repositories.ReturnRepository;
import com.orderreturn.service.audit.ReturnStateAuditService;
import com.orderreturn.service.job.RefundProcessingJob;
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
class ReturnServiceTest {
    @Mock ReturnRepository returnRepository;
    @Mock OrderRepository orderRepository;
    @Mock ReturnStateAuditService auditService;
    @Mock RefundProcessingJob refundProcessingJob;
    @InjectMocks ReturnService returnService;

    UUID orderId;
    UUID returnId;
    Order order;
    Return ret;

    @BeforeEach
    void setup() {
        orderId = UUID.randomUUID();
        returnId = UUID.randomUUID();
        order = Order.builder().id(orderId).state(OrderState.DELIVERED).build();
        ret = Return.builder().id(returnId).orderId(orderId).state(ReturnState.REQUESTED).refundStatus(RefundStatus.PENDING).build();
    }

    @Test
    void createReturn_forDeliveredOrder_succeeds() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(returnRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(returnRepository.save(any(Return.class))).thenAnswer(inv -> inv.getArgument(0));
        Return created = returnService.createReturn(orderId);
        assertEquals(ReturnState.REQUESTED, created.getState());
        assertEquals(RefundStatus.PENDING, created.getRefundStatus());
    }

    @Test
    void createReturn_notDelivered_throws() {
        order.setState(OrderState.PAID);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        assertThrows(IllegalStateException.class, () -> returnService.createReturn(orderId));
    }

    @Test
    void createReturn_duplicate_throws() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(returnRepository.findByOrderId(orderId)).thenReturn(Optional.of(ret));
        assertThrows(IllegalStateException.class, () -> returnService.createReturn(orderId));
    }

    @Test
    void transitionReturn_validTransition_updatesStateAndAudits() {
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(ret));
        when(returnRepository.save(any(Return.class))).thenAnswer(inv -> inv.getArgument(0));
        Return transitioned = returnService.transitionReturn(returnId, ReturnAction.APPROVE, ActorType.ADMIN);
        assertEquals(ReturnState.APPROVED, transitioned.getState());
        verify(auditService).auditReturnStateTransition(eq(returnId), eq(ReturnState.REQUESTED), eq(ReturnState.APPROVED), eq(ActorType.ADMIN));
        verify(refundProcessingJob, never()).processRefundAsync(any());
    }

    @Test
    void transitionReturn_toCompleted_triggersRefundJob() {
        ret.setState(ReturnState.RECEIVED);
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(ret));
        when(returnRepository.save(any(Return.class))).thenAnswer(inv -> inv.getArgument(0));
        Return transitioned = returnService.transitionReturn(returnId, ReturnAction.COMPLETE, ActorType.ADMIN);
        assertEquals(ReturnState.COMPLETED, transitioned.getState());
        verify(refundProcessingJob).processRefundAsync(returnId);
    }

    @Test
    void transitionReturn_fromTerminal_throws() {
        ret.setState(ReturnState.REJECTED);
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(ret));
        assertThrows(IllegalStateException.class, () -> returnService.transitionReturn(returnId, ReturnAction.APPROVE, ActorType.ADMIN));
    }

    @Test
    void transitionReturn_invalidTransition_throws() {
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(ret));
        assertThrows(IllegalStateException.class, () -> returnService.transitionReturn(returnId, ReturnAction.MARK_IN_TRANSIT, ActorType.ADMIN));
    }

    @Test
    void getReturnById_found_returnsReturn() {
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(ret));
        Return found = returnService.getReturnById(returnId);
        assertEquals(returnId, found.getId());
    }

    @Test
    void getReturnById_notFound_throws() {
        when(returnRepository.findById(returnId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> returnService.getReturnById(returnId));
    }
}

