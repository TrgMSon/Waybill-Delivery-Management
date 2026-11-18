package com.trgmson.backend.dto.shipment;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {
    private Long id;
    private String trackingCode;
    private String senderName;
    private String senderPhone;
    private String pickupAddress;
    private String receiverName;
    private String receiverPhone;
    private String deliveryAddress;
    private BigDecimal codAmount;
    private BigDecimal shippingFee;
    private String status;
    private String note;
    private Instant createdAt;
    private Instant deliveredAt;
}