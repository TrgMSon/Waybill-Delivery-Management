package com.trgmson.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trgmson.backend.model.ShipmentStatusHistory;

public interface ShipmentStatusHistoryRepository extends JpaRepository<ShipmentStatusHistory, Long> {
    
}
