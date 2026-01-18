package com.orderreturn.service.state;

import com.orderreturn.enums.OrderAction;
import com.orderreturn.enums.OrderState;

public class OrderStateMachine {
    public static OrderState transition(OrderState current, OrderAction action) {
        if (current == null || action == null) {
            throw new IllegalArgumentException("Current state and action must not be null");
        }
        switch (action) {
            case PAY:
                if (current == OrderState.PENDING_PAYMENT) {
                    return OrderState.PAID;
                }
                break;
            case CANCEL:
                if (current == OrderState.PENDING_PAYMENT || current == OrderState.PAID) {
                    return OrderState.CANCELLED;
                }
                break;
            case PROCESS:
                if (current == OrderState.PAID) {
                    return OrderState.PROCESSING_IN_WAREHOUSE;
                }
                break;
            case SHIP:
                if (current == OrderState.PROCESSING_IN_WAREHOUSE) {
                    return OrderState.SHIPPED;
                }
                break;
            case DELIVER:
                if (current == OrderState.SHIPPED) {
                    return OrderState.DELIVERED;
                }
                break;
        }
        throw new IllegalStateException("Invalid transition: " + current + " + " + action);
    }
}

