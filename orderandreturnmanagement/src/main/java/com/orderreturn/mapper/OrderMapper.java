package com.orderreturn.mapper;

import com.orderreturn.dto.OrderCreateResponse;
import com.orderreturn.entities.Order;

public class OrderMapper {
    public static OrderCreateResponse toOrderCreateResponse(Order order) {
        if (order == null) return null;
        return new OrderCreateResponse(
            order.getId(),
            order.getState(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
}
