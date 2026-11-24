package com.trgmson.backend.service.impl;

import java.time.Instant;
import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.trgmson.backend.dto.auth.AuthResponse;
import com.trgmson.backend.dto.auth.LoginRequest;
import com.trgmson.backend.dto.auth.RegisterRequest;
import com.trgmson.backend.dto.user.UserResponse;
import com.trgmson.backend.exception.ConflictException;
import com.trgmson.backend.exception.UnauthorizedException;
import com.trgmson.backend.model.RefreshToken;
import com.trgmson.backend.model.Driver;
import com.trgmson.backend.model.User;
import com.trgmson.backend.model.enums.Role;
import com.trgmson.backend.model.enums.UserStatus;
import com.trgmson.backend.repository.DriverRepository;
import com.trgmson.backend.repository.UserRepository;
import com.trgmson.backend.security.JwtService;
import com.trgmson.backend.service.AuthService;
import com.trgmson.backend.service.RefreshTokenService;

import jakarta.transaction.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final DriverRepository driverRepository;

    public AuthServiceImpl(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService, DriverRepository driverRepository) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.driverRepository = driverRepository;
    }

    @Override
    @Transactional
    public void register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException(
                    "Email này đã được đăng ký trong hệ thống. Vui lòng đăng nhập hoặc sử dụng email khác.");
        }

        String passwordHash = passwordEncoder.encode(req.getPassword());

        User user = User.builder()
                .email(req.getEmail())
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .role(Role.DRIVER)
                .status(UserStatus.ACTIVE)
                .passwordHash(passwordHash)
                .createdAt(Instant.now())
                .build();

        User savedUser = userRepository.save(Objects.requireNonNull(user, "Không tìm thấy người dùng"));
        driverRepository.save(Driver.builder()
                .user(savedUser)
                .build());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email hoặc mật khẩu không chính xác"));

        boolean isValidPass = passwordEncoder.matches(req.getPassword(), user.getPasswordHash());
        if (!isValidPass) {
            throw new UnauthorizedException("Email hoặc mật khẩu không chính xác");
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new UnauthorizedException("Tài khoản đã bị khóa hoặc không tồn tại.");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .userResponse(userResponse)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken oldToken = refreshTokenService.validateAndGet(refreshToken);
        if (oldToken.getUser().getStatus() == UserStatus.LOCKED) {
            throw new UnauthorizedException("Tài khoản đã bị khóa hoặc không tồn tại.");
        }
        String newAccessToken = jwtService.generateToken(oldToken.getUser());
        String newRefreshToken = refreshTokenService.rotate(oldToken);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .role(oldToken.getUser().getRole().name())
                .userResponse(UserResponse.builder()
                        .id(oldToken.getUser().getId())
                        .email(oldToken.getUser().getEmail())
                        .phone(oldToken.getUser().getPhone())
                        .role(oldToken.getUser().getRole().name())
                        .build())
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenService.validateAndGet(refreshToken);
        refreshTokenService.revoke(token);
    }
}

