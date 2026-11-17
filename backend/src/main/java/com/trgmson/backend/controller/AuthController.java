package com.trgmson.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trgmson.backend.common.ApiResponse;
import com.trgmson.backend.dto.auth.AuthResponse;
import com.trgmson.backend.dto.auth.LoginRequest;
import com.trgmson.backend.dto.auth.LogoutRequest;
import com.trgmson.backend.dto.auth.RefreshTokenRequest;
import com.trgmson.backend.dto.auth.RegisterRequest;
import com.trgmson.backend.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        ApiResponse<AuthResponse> res = ApiResponse.success("Đăng ký tài khoản thành công!", null);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody @Valid LoginRequest req) {
        ApiResponse<AuthResponse> res = ApiResponse.success("Login successfully ", authService.login(req));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody RefreshTokenRequest req) {
        String refreshToken = req.getRefreshToken();
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Access Token is  refreshed", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest req) {
        authService.logout(req.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
    }
}

