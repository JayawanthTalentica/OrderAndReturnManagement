package com.orderreturn.dto;

import com.orderreturn.enums.OrderState;
import com.orderreturn.enums.ActorType;
import java.time.Instant;
import java.util.UUID;

public class OrderStateHistoryResponse {
    private UUID id;
    private UUID orderId;
    private OrderState previousState;
    private OrderState newState;
    private ActorType actorType;
    private Instant timestamp;

    public OrderStateHistoryResponse() {}

    public OrderStateHistoryResponse(UUID id, UUID orderId, OrderState previousState, OrderState newState, ActorType actorType, Instant timestamp) {
        this.id = id;
        this.orderId = orderId;
        this.previousState = previousState;
        this.newState = newState;
        this.actorType = actorType;
        this.timestamp = timestamp;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public OrderState getPreviousState() { return previousState; }
    public void setPreviousState(OrderState previousState) { this.previousState = previousState; }
    public OrderState getNewState() { return newState; }
    public void setNewState(OrderState newState) { this.newState = newState; }
    public ActorType getActorType() { return actorType; }
    public void setActorType(ActorType actorType) { this.actorType = actorType; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
