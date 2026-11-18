package com.trgmson.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trgmson.backend.model.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    
}
