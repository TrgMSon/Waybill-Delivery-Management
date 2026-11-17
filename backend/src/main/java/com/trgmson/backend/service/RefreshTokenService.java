package com.trgmson.backend.service;

import com.trgmson.backend.model.RefreshToken;
import com.trgmson.backend.model.User;

public interface RefreshTokenService {
    String createRefreshToken(User user);
    RefreshToken validateAndGet(String rawToken);
    void revoke(RefreshToken entity);
    String rotate(RefreshToken oldToken);
}
