package com.orderreturn.dto;

import java.time.Instant;
import java.util.UUID;
import com.orderreturn.enums.ReturnState;
import com.orderreturn.enums.RefundStatus;

public class ReturnCreateResponse {
    private UUID id;
    private UUID orderId;
    private ReturnState state;
    private RefundStatus refundStatus;
    private Instant createdAt;
    private Instant updatedAt;

    public ReturnCreateResponse() {}

    public ReturnCreateResponse(UUID id, UUID orderId, ReturnState state, RefundStatus refundStatus, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.state = state;
        this.refundStatus = refundStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public ReturnState getState() { return state; }
    public void setState(ReturnState state) { this.state = state; }
    public RefundStatus getRefundStatus() { return refundStatus; }
    public void setRefundStatus(RefundStatus refundStatus) { this.refundStatus = refundStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

