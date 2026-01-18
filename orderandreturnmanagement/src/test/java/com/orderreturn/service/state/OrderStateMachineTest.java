package com.orderreturn.service.state;

import com.orderreturn.enums.OrderAction;
import com.orderreturn.enums.OrderState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderStateMachineTest {
    @Test
    void validTransitions() {
        assertEquals(OrderState.PAID, OrderStateMachine.transition(OrderState.PENDING_PAYMENT, OrderAction.PAY));
        assertEquals(OrderState.PROCESSING_IN_WAREHOUSE, OrderStateMachine.transition(OrderState.PAID, OrderAction.PROCESS));
        assertEquals(OrderState.SHIPPED, OrderStateMachine.transition(OrderState.PROCESSING_IN_WAREHOUSE, OrderAction.SHIP));
        assertEquals(OrderState.DELIVERED, OrderStateMachine.transition(OrderState.SHIPPED, OrderAction.DELIVER));
        assertEquals(OrderState.CANCELLED, OrderStateMachine.transition(OrderState.PENDING_PAYMENT, OrderAction.CANCEL));
        assertEquals(OrderState.CANCELLED, OrderStateMachine.transition(OrderState.PAID, OrderAction.CANCEL));
    }

    @Test
    void invalidBackwardTransitionThrows() {
        assertThrows(IllegalStateException.class, () -> OrderStateMachine.transition(OrderState.PAID, OrderAction.PAY));
        assertThrows(IllegalStateException.class, () -> OrderStateMachine.transition(OrderState.SHIPPED, OrderAction.SHIP));
    }

    @Test
    void transitionFromTerminalThrows() {
        assertThrows(IllegalStateException.class, () -> OrderStateMachine.transition(OrderState.CANCELLED, OrderAction.PAY));
        assertThrows(IllegalStateException.class, () -> OrderStateMachine.transition(OrderState.DELIVERED, OrderAction.SHIP));
    }

    @Test
    void nullStateOrActionThrows() {
        assertThrows(IllegalArgumentException.class, () -> OrderStateMachine.transition(null, OrderAction.PAY));
        assertThrows(IllegalArgumentException.class, () -> OrderStateMachine.transition(OrderState.PAID, null));
    }
}
