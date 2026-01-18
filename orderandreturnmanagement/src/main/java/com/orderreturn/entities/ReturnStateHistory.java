package com.orderreturn.entities;

import com.orderreturn.enums.ReturnState;
import com.orderreturn.enums.ActorType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "return_state_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnStateHistory {
    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "return_id", nullable = false, updatable = false)
    private UUID returnId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_state", nullable = false, updatable = false)
    private ReturnState previousState;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_state", nullable = false, updatable = false)
    private ReturnState newState;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, updatable = false)
    private ActorType actorType;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;
}

// ReturnState and ActorType enums will be implemented in TASK-007
