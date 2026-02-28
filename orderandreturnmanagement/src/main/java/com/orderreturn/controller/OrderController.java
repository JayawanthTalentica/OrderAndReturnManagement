package com.orderreturn.controller;

import com.orderreturn.dto.OrderCreateResponse;
import com.orderreturn.dto.OrderStateHistoryResponse;
import com.orderreturn.dto.OrderStateTransitionRequest;
import com.orderreturn.dto.PageResponse;
import com.orderreturn.entities.Order;
import com.orderreturn.enums.ActorType;
import com.orderreturn.mapper.OrderMapper;
import com.orderreturn.service.OrderService;
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
@RequestMapping("/api/orders")
@Validated
public class OrderController {
    private final OrderService orderService;
    private final OrderStateAuditService orderStateAuditService;

    public OrderController(OrderService orderService, OrderStateAuditService orderStateAuditService) {
        this.orderService = orderService;
        this.orderStateAuditService = orderStateAuditService;
    }

    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder() {
        Order order = orderService.createOrder();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderMapper.toOrderCreateResponse(order));
    }

    @PostMapping("/{orderId}/transition")
    public ResponseEntity<OrderCreateResponse> transitionOrderState(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderStateTransitionRequest request
    ) {
        // Hardcode actor type as USER for now
        Order order = orderService.transitionOrder(orderId, request.getAction(), ActorType.USER);
        return ResponseEntity.ok(OrderMapper.toOrderCreateResponse(order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderCreateResponse> getOrderById(@PathVariable UUID orderId) {
        Order order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(OrderMapper.toOrderCreateResponse(order));
    }

    @GetMapping("/{orderId}/history")
    public ResponseEntity<PageResponse<OrderStateHistoryResponse>> getOrderHistory(
            @PathVariable UUID orderId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        PageResponse<OrderStateHistoryResponse> history = orderStateAuditService.getOrderHistory(orderId, page, size);
        return ResponseEntity.ok(history);
    }
}
