package com.trgmson.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Không đúng định dạng email")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
