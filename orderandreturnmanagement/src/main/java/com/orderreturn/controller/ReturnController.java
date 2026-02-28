package com.orderreturn.controller;

import com.orderreturn.dto.ReturnCreateResponse;
import com.orderreturn.dto.ReturnStateTransitionRequest;
import com.orderreturn.dto.CreateReturnRequest;
import com.orderreturn.dto.ReturnStateHistoryResponse;
import com.orderreturn.dto.PageResponse;
import com.orderreturn.entities.Return;
import com.orderreturn.enums.ActorType;
import com.orderreturn.mapper.ReturnMapper;
import com.orderreturn.service.ReturnService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/returns")
@Validated
public class ReturnController {
    private final ReturnService returnService;
    private final ReturnStateAuditService returnStateAuditService;

    public ReturnController(ReturnService returnService, ReturnStateAuditService returnStateAuditService) {
        this.returnService = returnService;
        this.returnStateAuditService = returnStateAuditService;
    }

    @PostMapping
    public ResponseEntity<ReturnCreateResponse> createReturn(@RequestBody @Valid CreateReturnRequest request) {
        Return ret = returnService.createReturn(request.getOrderId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReturnMapper.toReturnCreateResponse(ret));
    }

    @PostMapping("/{returnId}/transition")
    public ResponseEntity<ReturnCreateResponse> transitionReturnState(
            @PathVariable UUID returnId,
            @Valid @RequestBody ReturnStateTransitionRequest request
    ) {
        // Hardcode actor type as ADMIN for now
        Return ret = returnService.transitionReturn(returnId, request.getAction(), ActorType.ADMIN);
        return ResponseEntity.ok(ReturnMapper.toReturnCreateResponse(ret));
    }

    @GetMapping("/{returnId}")
    public ResponseEntity<ReturnCreateResponse> getReturnById(@PathVariable UUID returnId) {
        Return ret = returnService.getReturnById(returnId);
        return ResponseEntity.ok(ReturnMapper.toReturnCreateResponse(ret));
    }

    @GetMapping("/{returnId}/history")
    public ResponseEntity<PageResponse<ReturnStateHistoryResponse>> getReturnHistory(
            @PathVariable UUID returnId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        PageResponse<ReturnStateHistoryResponse> history = returnStateAuditService.getReturnHistory(returnId, page, size);
        return ResponseEntity.ok(history);
    }
}
