package com.orderreturn.controller;

import com.orderreturn.dto.ReturnCreateResponse;
import com.orderreturn.dto.ReturnStateTransitionRequest;
import com.orderreturn.dto.CreateReturnRequest;
import com.orderreturn.entities.Return;
import com.orderreturn.enums.ActorType;
import com.orderreturn.mapper.ReturnMapper;
import com.orderreturn.service.ReturnService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {
    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
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
}
