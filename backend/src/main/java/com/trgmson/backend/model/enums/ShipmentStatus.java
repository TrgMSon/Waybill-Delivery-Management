package com.trgmson.backend.model.enums;

import java.util.Set;

import com.trgmson.backend.exception.ConflictException;

public enum ShipmentStatus {
    CREATED, ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED,
    DELIVERY_FAILED, RETURNING, RETURNED, CANCELLED;

    public boolean canTransitionTo(ShipmentStatus next) {
        return switch (this) {
            case CREATED -> Set.of(ASSIGNED, CANCELLED).contains(next);
            case ASSIGNED -> Set.of(PICKED_UP, CREATED, CANCELLED).contains(next); // CREATED nếu tài xế từ chối/hủy nhận
            case PICKED_UP -> Set.of(IN_TRANSIT, DELIVERED, DELIVERY_FAILED).contains(next);
            case IN_TRANSIT -> Set.of(DELIVERED, DELIVERY_FAILED).contains(next);
            case DELIVERY_FAILED -> Set.of(IN_TRANSIT, CANCELLED).contains(next); // Giao lại hoặc hoàn hủy
            case DELIVERED, CANCELLED -> false; // Trạng thái kết thúc (Terminal states)
            default -> throw new ConflictException("Unexpected value: " + this);
        };
    }
}
