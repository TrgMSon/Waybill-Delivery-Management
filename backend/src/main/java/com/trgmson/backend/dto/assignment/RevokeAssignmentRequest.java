package com.trgmson.backend.dto.assignment;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevokeAssignmentRequest {
    @Size(max = 500, message = "Lý do thu hồi tối đa 500 ký tự")
    private String reason;
}
