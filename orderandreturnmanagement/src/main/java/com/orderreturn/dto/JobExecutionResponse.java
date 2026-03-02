package com.orderreturn.dto;

import com.orderreturn.enums.JobType;
import com.orderreturn.enums.JobStatus;
import java.time.Instant;
import java.util.UUID;

public class JobExecutionResponse {
    private UUID id;
    private JobType jobType;
    private UUID relatedEntityId;
    private JobStatus status;
    private int attempts;
    private String lastError;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public JobType getJobType() { return jobType; }
    public void setJobType(JobType jobType) { this.jobType = jobType; }
    public UUID getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(UUID relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
