package com.orderreturn.service;

import com.orderreturn.entities.Order;
import com.orderreturn.entities.Return;
import com.orderreturn.enums.ActorType;
import com.orderreturn.enums.OrderState;
import com.orderreturn.enums.RefundStatus;
import com.orderreturn.enums.ReturnAction;
import com.orderreturn.enums.ReturnState;
import com.orderreturn.repositories.OrderRepository;
import com.orderreturn.repositories.ReturnRepository;
import com.orderreturn.service.audit.ReturnStateAuditService;
import com.orderreturn.service.job.RefundProcessingJob;
import com.orderreturn.service.state.ReturnStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ReturnService {
    private final ReturnRepository returnRepository;
    private final OrderRepository orderRepository;
    private final ReturnStateAuditService auditService;
    private final RefundProcessingJob refundProcessingJob;

    public ReturnService(ReturnRepository returnRepository, OrderRepository orderRepository, ReturnStateAuditService auditService, RefundProcessingJob refundProcessingJob) {
        this.returnRepository = returnRepository;
        this.orderRepository = orderRepository;
        this.auditService = auditService;
        this.refundProcessingJob = refundProcessingJob;
    }

    public Return createReturn(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (order.getState() != OrderState.DELIVERED) {
            throw new IllegalStateException("Return can only be requested for DELIVERED orders");
        }
        if (returnRepository.findByOrderId(orderId).isPresent()) {
            throw new IllegalStateException("A return already exists for this order");
        }
        Return ret = Return.builder()
                .orderId(orderId)
                .state(ReturnState.REQUESTED)
                .refundStatus(RefundStatus.PENDING)
                .build();
        return returnRepository.save(ret);
    }

    @Transactional
    public Return transitionReturn(UUID returnId, ReturnAction action, ActorType actorType) {
        Return ret = returnRepository.findById(returnId)
                .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));
        if (isTerminal(ret.getState())) {
            throw new IllegalStateException("Cannot transition from terminal state: " + ret.getState());
        }
        ReturnState currentState = ret.getState();
        ReturnState newState = ReturnStateMachine.transition(currentState, action);
        ret.setState(newState);
        returnRepository.save(ret);
        auditService.auditReturnStateTransition(returnId, currentState, newState, actorType);
        if (newState == ReturnState.COMPLETED) {
            triggerRefundProcessingJob(returnId);
        }
        return ret;
    }

    private boolean isTerminal(ReturnState state) {
        return state == ReturnState.REJECTED || state == ReturnState.COMPLETED;
    }

    // Placeholder for background job trigger (implementation in later task)
    private void triggerRefundProcessingJob(UUID returnId) {
        refundProcessingJob.processRefundAsync(returnId);
    }

    public Return getReturnById(UUID returnId) {
        return returnRepository.findById(returnId)
                .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));
    }
}
