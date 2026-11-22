package com.trgmson.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trgmson.backend.model.Shipment;
import com.trgmson.backend.model.enums.ShipmentStatus;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);

    boolean existsByTrackingCode(String trackingCode);

    Optional<Shipment> findByTrackingCode(String trackingCode);

    Page<Shipment> findByAssignedDriverId(Long id, Pageable pageable);

    Page<Shipment> findByAssignedDriverIdAndStatus(Long id, ShipmentStatus status, Pageable pageable);

    boolean existsById(Long id);
}
