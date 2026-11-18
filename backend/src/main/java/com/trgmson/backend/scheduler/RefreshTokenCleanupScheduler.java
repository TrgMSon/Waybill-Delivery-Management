package com.trgmson.backend.scheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.trgmson.backend.repository.RefreshTokenRepository;

@Component
public class RefreshTokenCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupScheduler.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupScheduler(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // Chạy vào 03:00:00 sáng mỗi ngày theo giờ Việt Nam
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void cleanupStaleTokens() {
        Instant now = Instant.now();
        // Giữ lại các token đã bị revoke trong vòng 7 ngày gần nhất để phục vụ kiểm tra/audit (nếu cần),
        // chỉ xóa hẳn những token đã revoke quá 7 ngày hoặc đã hết hạn.
        Instant revokedRetentionCutoff = now.minus(7, ChronoUnit.DAYS);

        log.info("Bắt đầu tác vụ dọn dẹp Refresh Token rác...");
        int deletedCount = refreshTokenRepository.deleteStaleTokens(now, revokedRetentionCutoff);
        log.info("Hoàn tất dọn dẹp Refresh Token: đã xóa {} bản ghi.", deletedCount);
    }
}