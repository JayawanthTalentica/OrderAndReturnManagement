package com.orderreturn.controller;

import com.orderreturn.dto.OrderCreateResponse;
import com.orderreturn.dto.OrderStateTransitionRequest;
import com.orderreturn.entities.Order;
import com.orderreturn.enums.ActorType;
import com.orderreturn.mapper.OrderMapper;
import com.orderreturn.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
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
}
