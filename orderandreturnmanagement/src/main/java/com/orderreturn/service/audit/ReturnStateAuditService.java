package com.orderreturn.service.audit;

import com.orderreturn.dto.PageResponse;
import com.orderreturn.dto.ReturnStateHistoryResponse;
import com.orderreturn.entities.ReturnEntity;
import com.orderreturn.entities.ReturnStateHistory;
import com.orderreturn.enums.ActorType;
import com.orderreturn.enums.ReturnState;
import com.orderreturn.exception.NotFoundException;
import com.orderreturn.repositories.ReturnRepository;
import com.orderreturn.repositories.ReturnStateHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReturnStateAuditService {
    private final ReturnStateHistoryRepository historyRepository;
    private final ReturnRepository returnRepository;

    public ReturnStateAuditService(ReturnStateHistoryRepository historyRepository, ReturnRepository returnRepository) {
        this.historyRepository = historyRepository;
        this.returnRepository = returnRepository;
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

    public PageResponse<ReturnStateHistoryResponse> getReturnHistory(UUID returnId, int page, int size) {
        var ret = returnRepository.findById(returnId)
                .orElseThrow(() -> new NotFoundException("Return not found"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").ascending());
        Page<ReturnStateHistory> historyPage = historyRepository.findByReturnId(returnId, pageable);
        List<ReturnStateHistoryResponse> content = historyPage.getContent().stream()
                .map(h -> new ReturnStateHistoryResponse(
                        h.getId(),
                        h.getReturnId(),
                        h.getPreviousState(),
                        h.getNewState(),
                        h.getActorType(),
                        h.getTimestamp()
                ))
                .collect(Collectors.toList());
        return new PageResponse<>(content, historyPage.getTotalElements(), historyPage.getTotalPages());
    }
}
