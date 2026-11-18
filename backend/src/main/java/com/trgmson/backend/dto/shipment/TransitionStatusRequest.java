package com.trgmson.backend.dto.shipment;

import com.trgmson.backend.model.enums.ShipmentStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransitionStatusRequest {

    @NotNull(message = "Trạng thái mới không được để trống")
    private ShipmentStatus nextStatus;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}