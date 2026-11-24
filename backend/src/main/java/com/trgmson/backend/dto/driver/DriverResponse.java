package com.trgmson.backend.dto.driver;

import java.math.BigDecimal;

public record DriverResponse(
        Long id,
        Long userId,
        String fullName,
        String email,
        String phone,
        String vehicleType,
        String licensePlate,
        String serviceArea,
        BigDecimal maxLoadKg,
        Integer maxActiveShipments,
        String status) {
}
