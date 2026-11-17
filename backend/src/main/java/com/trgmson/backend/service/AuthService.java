package com.trgmson.backend.service;

import com.trgmson.backend.dto.auth.AuthResponse;
import com.trgmson.backend.dto.auth.LoginRequest;
import com.trgmson.backend.dto.auth.RegisterRequest;

public interface AuthService {
    public void register(RegisterRequest req);
    public AuthResponse login(LoginRequest req);
    public AuthResponse refreshToken(String refreshToken);
    public void logout(String refreshToken);
}
