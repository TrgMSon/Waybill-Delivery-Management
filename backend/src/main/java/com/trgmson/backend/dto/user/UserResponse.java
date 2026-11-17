package com.trgmson.backend.dto.user;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class UserResponse {
    private long id;
    private String email;
    private String phone;
    private String role;

    // Constructor đầy đủ tham số
    public UserResponse(long id, String email, String phone, String role) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }
}
