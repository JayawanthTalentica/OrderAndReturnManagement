package com.orderreturn.dto;

import com.orderreturn.enums.OrderState;
import java.util.UUID;
import java.time.Instant;

public class OrderCreateResponse {
    private UUID id;
    private OrderState state;
    private Instant createdAt;
    private Instant updatedAt;

    public OrderCreateResponse() {}

    public OrderCreateResponse(UUID id, OrderState state, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.state = state;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public OrderState getState() { return state; }
    public void setState(OrderState state) { this.state = state; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
