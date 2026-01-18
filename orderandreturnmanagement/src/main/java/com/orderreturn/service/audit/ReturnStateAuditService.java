package com.orderreturn.service.audit;

import com.orderreturn.entities.ReturnStateHistory;
import com.orderreturn.enums.ActorType;
import com.orderreturn.enums.ReturnState;
import com.orderreturn.repositories.ReturnStateHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReturnStateAuditService {
    private final ReturnStateHistoryRepository historyRepository;

    public ReturnStateAuditService(ReturnStateHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Transactional
    public void auditReturnStateTransition(UUID returnId, ReturnState previousState, ReturnState newState, ActorType actorType) {
        ReturnStateHistory history = ReturnStateHistory.builder()
                .returnId(returnId)
                .previousState(previousState)
                .newState(newState)
                .actorType(actorType)
                .build();
        historyRepository.save(history);
    }
}

