package com.trgmson.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.trgmson.backend.dto.assignment.AssignmentResponse;
import com.trgmson.backend.model.User;
import com.trgmson.backend.model.enums.AssignmentStatus;

public interface AssignmentService {
    // admin actions
    AssignmentResponse assignShipmentToDriver(Long shipmentId, Long driverId, User user);
    AssignmentResponse reassignShipmentToDriver(Long shipmentId, Long driverId, User user);
    void revokeAssignment(Long shipmentId, User user, String reason);

    // driver actions
    AssignmentResponse acceptAssignment(Long assignmentId, User user);
    AssignmentResponse rejectAssignment(Long assignmentId, Long shipmentId, User user, String reason);

    // shedule 
    int expireStaleAssignments();

    // get data
    List<AssignmentResponse> getAssignmentsByShipment(Long shipmentId);
    Page<AssignmentResponse> getDriverAssignments(Long id, AssignmentStatus status, Pageable pageable);
    AssignmentResponse getAssignmentById(Long assignmentId);

}
