package com.orderreturn.mapper;

import com.orderreturn.entities.Return;
import com.orderreturn.dto.ReturnCreateResponse;

public class ReturnMapper {
    public static ReturnCreateResponse toReturnCreateResponse(Return entity) {
        if (entity == null) return null;
        return new ReturnCreateResponse(
            entity.getId(),
            entity.getOrderId(),
            entity.getState(),
            entity.getRefundStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}

