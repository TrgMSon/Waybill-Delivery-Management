package com.trgmson.backend.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.trgmson.backend.model.enums.DriverStatus;
import com.trgmson.backend.model.enums.ShipmentStatus;
import com.trgmson.backend.repository.AssignmentRepository;
import com.trgmson.backend.repository.DriverRepository;
import com.trgmson.backend.repository.ShipmentRepository;
import com.trgmson.backend.service.AssignmentService;
import com.trgmson.backend.service.ShipmentService;

import jakarta.transaction.Transactional;

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

        if (!assignmentRepository.existsByShipmentId(shipmentId)) {
            Assignment assignment = new Assignment();
            assignment.setShipment(shipmentOpt.get());
            assignment.setDriver(driverOpt.get());
            assignment.setStatus(AssignmentStatus.OFFERED);
            assignmentRepository.save(assignment);

            shipmentService.transitionStatus(shipmentId, ShipmentStatus.ASSIGNED, user, "");

            return new AssignmentResponse(assignment.getId(), shipmentId, driverId, assignment.getStatus().toString());
        } else {
            throw new BadRequestException(
                    "Đơn hàng với ID " + shipmentId + " đã được phân công cho tài xế với ID " + driverId + ".");
        }
    }

    @Override
    public AssignmentResponse reassignShipmentToDriver(Long shipmentId, Long driverId, User user) {
        Optional<Assignment> assignmentOpt = assignmentRepository.findByShipmentIdAndStatusIn(shipmentId,
                List.of(AssignmentStatus.OFFERED, AssignmentStatus.ACCEPTED));
        if (assignmentOpt.isEmpty()) {
            throw new BadRequestException("Đơn hàng với ID " + shipmentId + " chưa được gán.");
        }

        Assignment assignment = assignmentOpt.get();
        if (assignment.getDriver().getId() == driverId) {
            throw new BadRequestException(
                    "Đơn hàng với ID " + shipmentId + " đã được phân công cho tài xế với ID " + driverId + ".");
        }

        Optional<Driver> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isEmpty()) {
            throw new BadRequestException("Tài xế với ID " + driverId + " không tồn tại.");
        }
        Driver driver = driverOpt.get();
        if (!driver.getStatus().equals(DriverStatus.AVAILABLE)) {
            throw new ConflictException("Tài xế với ID " + driverId + " không khả dụng.");
        }

        assignment.setStatus(AssignmentStatus.CANCELLED);
        assignmentRepository.save(assignment);
        assignShipmentToDriver(shipmentId, driverId, user);

        return new AssignmentResponse(assignment.getId(), shipmentId, driverId, assignment.getStatus().toString());
    }

    @Override
    public void revokeAssignment(Long shipmentId, User user, String reason) {
        Optional<Assignment> assignmentOpt = assignmentRepository.findByShipmentIdAndStatusIn(
                shipmentId,
                List.of(AssignmentStatus.OFFERED, AssignmentStatus.ACCEPTED));
        if (assignmentOpt.isEmpty()) {
            throw new BadRequestException("Đơn hàng với ID " + shipmentId + " chưa được gán.");
        }

        Assignment assignment = assignmentOpt.get();
        String shipmentStatus = assignment.getShipment().getStatus().toString();
        if (shipmentStatus.equals("PICKED_UP")) {
            throw new ConflictException("Đơn hàng với ID " + shipmentId + " đã được tài xế nhận, không thể thu hồi.");
        }

        assignment.setStatus(AssignmentStatus.CANCELLED);
        assignment.setReason(reason);
        assignment.setRespondedAt(Instant.now());
        assignment.setExpiresAt(Instant.now());
        assignment.setDriver(null);
        assignmentRepository.save(assignment);

        shipmentService.transitionStatus(shipmentId, ShipmentStatus.CREATED, user, reason);
    }

    @Override
    public AssignmentResponse acceptAssignment(Long assignmentId, User user) {
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

        return new AssignmentResponse(assignmentId, assignment.getShipment().getId(), assignment.getDriver().getId(),
                assignment.getStatus().toString());
    }

    @Override
    public AssignmentResponse rejectAssignment(Long assignmentId, Long shipmentId, User user, String reason) {
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if (assignmentOpt.isEmpty()) {
            throw new BadRequestException("Phân công với ID " + assignmentId + " không tồn tại.");
        }

        Assignment assignment = assignmentOpt.get();
        if (assignment.getDriver().getId() != user.getId()) {
            throw new BadRequestException("Tài xế không được gán cho đơn hàng với ID " + shipmentId);
        }
        if (assignment.getStatus().equals(AssignmentStatus.ACCEPTED)) {
            throw new ConflictException("Phân công đã được chấp nhận, không thể từ chối.");
        }
        if (assignment.getStatus().equals(AssignmentStatus.CANCELLED)) {
            throw new ConflictException("Phân công đã bị hủy, không thể từ chối.");
        }

        assignment.setReason(reason);
        assignment.setRespondedAt(Instant.now());
        assignment.setStatus(AssignmentStatus.REJECTED);
        assignment.setDriver(null);
        assignmentRepository.save(assignment);

        shipmentService.transitionStatus(shipmentId, ShipmentStatus.CREATED, user, reason);

        return new AssignmentResponse(assignmentId, shipmentId, shipmentId, assignment.getStatus().toString());
    }

    @Override
    @Transactional
    public int expireStaleAssignments() {
        Instant now = Instant.now();
        List<Assignment> staleAssignments = assignmentRepository
                .findByStatusAndExpiresAtBefore(AssignmentStatus.OFFERED, now);

        for (Assignment assignment : staleAssignments) {
            assignment.setStatus(AssignmentStatus.EXPIRED);
            assignment.setRespondedAt(now);
            assignment.setDriver(null);
            assignmentRepository.save(assignment);

            Shipment shipment = assignment.getShipment();

            // Chuyển trạng thái đơn về CREATED và ghi lịch sử (actor = null đại diện cho
            // System)
            shipmentService.transitionStatus(
                    shipment.getId(),
                    ShipmentStatus.CREATED,
                    null,
                    "Quá hạn phản hồi nhận đơn");
        }

        return staleAssignments.size();
    }

    @Override
    public List<AssignmentResponse> getAssignmentsByShipment(Long shipmentId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAssignmentsByShipment'");
    }

    @Override
    public Page<AssignmentResponse> getDriverAssignments(Long id, AssignmentStatus status, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDriverAssignments'");
    }

    @Override
    public AssignmentResponse getAssignmentById(Long assignmentId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAssignmentById'");
    }
}
