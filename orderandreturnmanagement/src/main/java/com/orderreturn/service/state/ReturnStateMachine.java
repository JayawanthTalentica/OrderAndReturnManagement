package com.orderreturn.service.state;

import com.orderreturn.enums.ReturnAction;
import com.orderreturn.enums.ReturnState;

public class ReturnStateMachine {
    public static ReturnState transition(ReturnState current, ReturnAction action) {
        if (current == null || action == null) {
            throw new IllegalArgumentException("Current state and action must not be null");
        }
        switch (action) {
            case APPROVE:
                if (current == ReturnState.REQUESTED) {
                    return ReturnState.APPROVED;
                }
                break;
            case REJECT:
                if (current == ReturnState.REQUESTED) {
                    return ReturnState.REJECTED;
                }
                break;
            case MARK_IN_TRANSIT:
                if (current == ReturnState.APPROVED) {
                    return ReturnState.IN_TRANSIT;
                }
                break;
            case RECEIVE:
                if (current == ReturnState.IN_TRANSIT) {
                    return ReturnState.RECEIVED;
                }
                break;
            case COMPLETE:
                if (current == ReturnState.RECEIVED) {
                    return ReturnState.COMPLETED;
                }
                break;
        }
        // Terminal state: REJECTED cannot transition
        if (current == ReturnState.REJECTED) {
            throw new IllegalStateException("Return is REJECTED and cannot transition");
        }
        // COMPLETED is also terminal
        if (current == ReturnState.COMPLETED) {
            throw new IllegalStateException("Return is COMPLETED and cannot transition");
        }
        throw new IllegalStateException("Invalid transition: " + current + " + " + action);
    }
}

