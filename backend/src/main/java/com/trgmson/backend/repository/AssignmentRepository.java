package com.trgmson.backend.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trgmson.backend.model.Assignment;
import com.trgmson.backend.model.enums.AssignmentStatus;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    boolean existsByShipmentIdAndDriverUserId(Long shipmentId, Long userId);

    boolean existsByShipmentIdAndDriverIdAndStatusIn(Long shipmentId, Long driverId, List<AssignmentStatus> statuses);

    Optional<Assignment> findByShipmentIdAndStatusIn(Long shipmentId, List<AssignmentStatus> statuses);

    Optional<Assignment> findByShipmentId(Long shipmentId);

    boolean existsByShipmentIdAndStatusIn(Long shipmentId, List<AssignmentStatus> statuses);

    List<Assignment> findByStatusAndExpiresAtBefore(AssignmentStatus offered, Instant now);

    List<Assignment> findAllByShipmentIdOrderByOfferedAtDesc(Long shipmentId);

    Page<Assignment> findByDriverIdAndStatus(
            Long driverId, AssignmentStatus status, Pageable pageable);

    Page<Assignment> findByDriverId(Long driverId, Pageable pageable);
}
