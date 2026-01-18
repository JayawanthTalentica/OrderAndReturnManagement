package com.orderreturn.entities;

import com.orderreturn.enums.OrderState;
import com.orderreturn.enums.ActorType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_state_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStateHistory {
    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_state", nullable = false, updatable = false)
    private OrderState previousState;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_state", nullable = false, updatable = false)
    private OrderState newState;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, updatable = false)
    private ActorType actorType;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;
}

// OrderState and ActorType enums will be implemented in TASK-007
