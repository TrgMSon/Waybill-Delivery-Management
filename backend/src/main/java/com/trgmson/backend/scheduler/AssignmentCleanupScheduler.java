package com.trgmson.backend.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trgmson.backend.service.AssignmentService;

@Component
public class AssignmentCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(AssignmentCleanupScheduler.class);
    private final AssignmentService assignmentService;

    public AssignmentCleanupScheduler(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    // Quét mỗi 1 phút một lần
    @Scheduled(fixedDelay = 60000)
    public void handleExpiredAssignments() {
        int expiredCount = assignmentService.expireStaleAssignments();
        if (expiredCount > 0) {
            log.info("Hệ thống đã tự động hủy {} lượt gán đơn quá hạn.", expiredCount);
        }
    }
}