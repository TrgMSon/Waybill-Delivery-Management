package com.trgmson.backend.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trgmson.backend.model.Assignment;
import com.trgmson.backend.model.enums.AssignmentStatus;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    boolean existsByShipmentIdAndDriverIdAndStatusIn(Long shipmentId, Long driverId, List<AssignmentStatus> statuses);

    Optional<Assignment> findByShipmentIdAndStatusIn(Long shipmentId, List<AssignmentStatus> statuses);

    Optional<Assignment> findByShipmentId(Long shipmentId);

    boolean existsByShipmentId(Long shipmentId);

    List<Assignment> findByStatusAndExpiresAtBefore(AssignmentStatus offered, Instant now);
}
