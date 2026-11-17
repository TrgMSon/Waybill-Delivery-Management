package com.trgmson.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trgmson.backend.model.Driver;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    
}
