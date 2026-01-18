package com.orderreturn.dto;

import com.orderreturn.enums.OrderAction;

public class OrderStateTransitionRequest {
    private OrderAction action;

    public OrderStateTransitionRequest() {}
    public OrderStateTransitionRequest(OrderAction action) { this.action = action; }
    public OrderAction getAction() { return action; }
    public void setAction(OrderAction action) { this.action = action; }
}
