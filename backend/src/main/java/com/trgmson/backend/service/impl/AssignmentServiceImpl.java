package com.trgmson.backend.service.impl;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.trgmson.backend.dto.assignment.AssignmentResponse;
import com.trgmson.backend.exception.NotFoundException;
import com.trgmson.backend.exception.BadRequestException;
import com.trgmson.backend.exception.ConflictException;
import com.trgmson.backend.model.Assignment;
import com.trgmson.backend.model.Driver;
import com.trgmson.backend.model.Shipment;
import com.trgmson.backend.model.User;
import com.trgmson.backend.model.enums.AssignmentStatus;
import com.trgmson.backend.model.enums.ShipmentStatus;
import com.trgmson.backend.repository.AssignmentRepository;
import com.trgmson.backend.repository.DriverRepository;
import com.trgmson.backend.repository.ShipmentRepository;
import com.trgmson.backend.service.AssignmentService;
import com.trgmson.backend.service.ShipmentService;

@Service
public class AssignmentServiceImpl implements AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final ShipmentRepository shipmentRepository;
    private final DriverRepository driverRepository;
    private final ShipmentService shipmentService;

    public AssignmentServiceImpl(AssignmentRepository assignmentRepository, ShipmentRepository shipmentRepository,
            DriverRepository driverRepository, ShipmentService shipmentService) {
        this.assignmentRepository = assignmentRepository;
        this.shipmentRepository = shipmentRepository;
        this.driverRepository = driverRepository;
        this.shipmentService = shipmentService;
    }

    @Override
    public AssignmentResponse assignShipmentToDriver(Long shipmentId, Long driverId, User user) {
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
        if (shipmentOpt.isEmpty()) {
            throw new NotFoundException("Đơn hàng với ID " + shipmentId + " không tồn tại.");
        }

        Optional<Driver> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isEmpty()) {
            throw new NotFoundException("Tài xế với ID " + driverId + " không tồn tại.");
        }

        if (!assignmentRepository.existsByShipmentIdAndUserId(shipmentId, driverId)) {
            Assignment assignment = new Assignment();
            assignment.setShipment(shipmentOpt.get());
            assignment.setDriver(driverOpt.get());
            assignment.setStatus(AssignmentStatus.OFFERED);
            assignmentRepository.save(assignment);

            shipmentService.transitionStatus(shipmentId, ShipmentStatus.ASSIGNED, user);

            return new AssignmentResponse(assignment.getId(), shipmentId, driverId, assignment.getStatus().toString());
        } else {
            throw new BadRequestException(
                    "Đơn hàng với ID " + shipmentId + " đã được phân công cho tài xế với ID " + driverId + ".");
        }
    }

    @Override
    public AssignmentResponse reassignShipmentToDriver(Long shipmentId, Long driverId, User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reassignShipmentToDriver'");
    }

    @Override
    public void revokeAssignment(Long shipmentId, User user, String reason) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'revokeAssignment'");
    }

    @Override
    public AssignmentResponse acceptAssignment(Long assignmentId, Long shipmentId, User user) {
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if (assignmentOpt.isEmpty()) {
            throw new NotFoundException("Phân công với ID " + assignmentId + " không tồn tại.");
        }

        Assignment assignment = assignmentOpt.get();
        if (assignment.getDriver().getId() != user.getId()) {
            throw new BadRequestException(
                    "Phân công với ID " + assignmentId + " không được gán cho tài xế có ID " + user.getId());
        }
        if (!assignment.getStatus().equals(AssignmentStatus.OFFERED)) {
            throw new ConflictException(
                    "Lượt gán không ở trạng thái chờ nhận (hiện tại: " + assignment.getStatus() + ")");
        }
        if (Instant.now().isAfter(assignment.getExpiresAt())) {
            throw new BadRequestException("Lời mời nhận đơn đã hết hạn");
        }

        assignment.setStatus(AssignmentStatus.ACCEPTED);
        assignmentRepository.save(assignment);

        return new AssignmentResponse(assignmentId, shipmentId, shipmentId, assignment.getStatus().toString());
    }

    @Override
    public AssignmentResponse rejectAssignment(Long assignmentId, Long shipmentId, User user, String reason) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rejectAssignment'");
    }
}
