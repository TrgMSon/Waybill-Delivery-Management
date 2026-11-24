package com.trgmson.backend.dto.assignment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignDriverRequest {
    @NotNull(message = "shipmentId không được để trống")
    private Long shipmentId;

    @NotNull(message = "driverId không được để trống")
    private Long driverId;
}
