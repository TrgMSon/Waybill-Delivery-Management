package com.trgmson.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trgmson.backend.model.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    
}
