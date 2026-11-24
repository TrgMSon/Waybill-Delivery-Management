package com.trgmson.backend.dto.assignment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RejectAssignmentRequest {
    @NotBlank(message = "Lý do từ chối không được để trống")
    @Size(max = 500, message = "Lý do từ chối tối đa 500 ký tự")
    private String reason;

    @NotNull(message = "ID đơn hàng không được để trống")
    private Long shipmentId;
}
