package com.orderreturn.entities;

import com.orderreturn.enums.JobType;
import com.orderreturn.enums.JobStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    indexes = {
        @Index(name = "idx_jobexecution_relatedEntityId", columnList = "relatedEntityId"),
        @Index(name = "idx_jobexecution_jobType", columnList = "jobType"),
        @Index(name = "idx_jobexecution_status", columnList = "status"),
        @Index(name = "idx_jobexecution_createdAt", columnList = "createdAt")
    }
)
public class JobExecution {
    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    private UUID relatedEntityId;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private int attempts;

    private String lastError;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // Getters and setters
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
