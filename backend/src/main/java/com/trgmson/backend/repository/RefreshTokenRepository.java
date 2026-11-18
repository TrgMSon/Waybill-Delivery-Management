package com.trgmson.backend.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trgmson.backend.model.RefreshToken;
import com.trgmson.backend.model.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String token);

    void deleteByUser(User user);

    @Modifying
    @Query("""
        DELETE FROM RefreshToken r
        WHERE (r.expiresAt < :expiredBefore)
           OR (r.revoked = true AND r.revokedAt < :revokedBefore)
        """)
    int deleteStaleTokens(
            @Param("expiredBefore") Instant expiredBefore,
            @Param("revokedBefore") Instant revokedBefore
    );

}
