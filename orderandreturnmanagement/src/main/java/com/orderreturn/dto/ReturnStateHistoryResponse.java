package com.orderreturn.dto;

import com.orderreturn.enums.ReturnState;
import com.orderreturn.enums.ActorType;
import java.time.Instant;
import java.util.UUID;

public class ReturnStateHistoryResponse {
    private UUID id;
    private UUID returnId;
    private ReturnState previousState;
    private ReturnState newState;
    private ActorType actorType;
    private Instant timestamp;

    public ReturnStateHistoryResponse() {}

    public ReturnStateHistoryResponse(UUID id, UUID returnId, ReturnState previousState, ReturnState newState, ActorType actorType, Instant timestamp) {
        this.id = id;
        this.returnId = returnId;
        this.previousState = previousState;
        this.newState = newState;
        this.actorType = actorType;
        this.timestamp = timestamp;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getReturnId() { return returnId; }
    public void setReturnId(UUID returnId) { this.returnId = returnId; }
    public ReturnState getPreviousState() { return previousState; }
    public void setPreviousState(ReturnState previousState) { this.previousState = previousState; }
    public ReturnState getNewState() { return newState; }
    public void setNewState(ReturnState newState) { this.newState = newState; }
    public ActorType getActorType() { return actorType; }
    public void setActorType(ActorType actorType) { this.actorType = actorType; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
