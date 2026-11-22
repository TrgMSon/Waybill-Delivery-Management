package com.trgmson.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trgmson.backend.model.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    boolean existsByShipmentIdAndDriverIdAndStatusIn(Long shipmentId, Long driverId, List<String> statuses);

    Optional<Assignment> findByShipmentIdAndStatusIn(Long shipmentId, List<String> statuses);

    Optional<Assignment> findByShipmentId(Long shipmentId);

    boolean existsByShipmentIdAndUserId(Long id, Long id2);

    
}
