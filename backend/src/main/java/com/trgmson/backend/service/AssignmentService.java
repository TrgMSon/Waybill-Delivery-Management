package com.trgmson.backend.service;

import com.trgmson.backend.dto.assignment.AssignmentResponse;
import com.trgmson.backend.model.User;

public interface AssignmentService {
    // admin actions
    AssignmentResponse assignShipmentToDriver(Long shipmentId, Long driverId, User user);
    AssignmentResponse reassignShipmentToDriver(Long shipmentId, Long driverId, User user);
    void revokeAssignment(Long shipmentId, User user, String reason);

    // driver actions
    AssignmentResponse acceptAssignment(Long assignmentId, Long shipmentId, User user);
    AssignmentResponse rejectAssignment(Long assignmentId, Long shipmentId, User user, String reason);
}
