package com.trgmson.backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trgmson.backend.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
// trả về khi login thành công
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String role;
    private UserResponse userResponse;
}