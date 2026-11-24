package com.trgmson.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trgmson.backend.model.Driver;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByUserId(Long userId);
}
