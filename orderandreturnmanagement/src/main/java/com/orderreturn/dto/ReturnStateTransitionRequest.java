package com.orderreturn.dto;

import com.orderreturn.enums.ReturnAction;

public class ReturnStateTransitionRequest {
    private ReturnAction action;

    public ReturnStateTransitionRequest() {}
    public ReturnStateTransitionRequest(ReturnAction action) { this.action = action; }
    public ReturnAction getAction() { return action; }
    public void setAction(ReturnAction action) { this.action = action; }
}
