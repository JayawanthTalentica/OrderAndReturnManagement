package com.orderreturn.service.state;

import com.orderreturn.enums.ReturnAction;
import com.orderreturn.enums.ReturnState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReturnStateMachineTest {
    @Test
    void validTransitions() {
        assertEquals(ReturnState.APPROVED, ReturnStateMachine.transition(ReturnState.REQUESTED, ReturnAction.APPROVE));
        assertEquals(ReturnState.REJECTED, ReturnStateMachine.transition(ReturnState.REQUESTED, ReturnAction.REJECT));
        assertEquals(ReturnState.IN_TRANSIT, ReturnStateMachine.transition(ReturnState.APPROVED, ReturnAction.MARK_IN_TRANSIT));
        assertEquals(ReturnState.RECEIVED, ReturnStateMachine.transition(ReturnState.IN_TRANSIT, ReturnAction.RECEIVE));
        assertEquals(ReturnState.COMPLETED, ReturnStateMachine.transition(ReturnState.RECEIVED, ReturnAction.COMPLETE));
    }

    @Test
    void transitionFromTerminalThrows() {
        assertThrows(IllegalStateException.class, () -> ReturnStateMachine.transition(ReturnState.REJECTED, ReturnAction.APPROVE));
        assertThrows(IllegalStateException.class, () -> ReturnStateMachine.transition(ReturnState.COMPLETED, ReturnAction.RECEIVE));
    }

    @Test
    void invalidTransitionThrows() {
        assertThrows(IllegalStateException.class, () -> ReturnStateMachine.transition(ReturnState.REQUESTED, ReturnAction.MARK_IN_TRANSIT));
        assertThrows(IllegalStateException.class, () -> ReturnStateMachine.transition(ReturnState.APPROVED, ReturnAction.RECEIVE));
    }

    @Test
    void nullStateOrActionThrows() {
        assertThrows(IllegalArgumentException.class, () -> ReturnStateMachine.transition(null, ReturnAction.APPROVE));
        assertThrows(IllegalArgumentException.class, () -> ReturnStateMachine.transition(ReturnState.REQUESTED, null));
    }
}

