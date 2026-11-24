package com.trgmson.backend.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

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
    private static final List<AssignmentStatus> ACTIVE_STATUSES =
            List.of(AssignmentStatus.OFFERED, AssignmentStatus.ACCEPTED);

    private final AssignmentRepository assignmentRepository;
    private final ShipmentRepository shipmentRepository;
    private final DriverRepository driverRepository;
    private final ShipmentService shipmentService;

    @Value("${assignment.response-timeout-minutes:30}")
    private long responseTimeoutMinutes;

    public AssignmentServiceImpl(AssignmentRepository assignmentRepository, ShipmentRepository shipmentRepository,
            DriverRepository driverRepository, ShipmentService shipmentService) {
        this.assignmentRepository = assignmentRepository;
        this.shipmentRepository = shipmentRepository;
        this.driverRepository = driverRepository;
        this.shipmentService = shipmentService;
    }

    @Override
    @Transactional
    public AssignmentResponse assignShipmentToDriver(Long shipmentId, Long driverId, User user) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new NotFoundException("Đơn hàng với ID " + shipmentId + " không tồn tại."));
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new NotFoundException("Tài xế với ID " + driverId + " không tồn tại."));

        if (assignmentRepository.existsByShipmentIdAndStatusIn(shipmentId, ACTIVE_STATUSES)) {
            throw new BadRequestException("Đơn hàng với ID " + shipmentId + " đã được phân công.");
        }

        Assignment assignment = new Assignment();
        assignment.setShipment(shipment);
        assignment.setDriver(driver);
        assignment.setStatus(AssignmentStatus.OFFERED);
        assignment.setExpiresAt(Instant.now().plus(responseTimeoutMinutes, ChronoUnit.MINUTES));
        assignmentRepository.save(assignment);
        shipment.setCurrentDriver(driver);
        shipmentRepository.save(shipment);
        shipmentService.transitionStatus(shipmentId, ShipmentStatus.ASSIGNED, user, "");

        return toResponse(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponse reassignShipmentToDriver(Long shipmentId, Long driverId, User user) {
        Assignment previous = assignmentRepository.findByShipmentIdAndStatusIn(shipmentId, ACTIVE_STATUSES)
                .orElseThrow(() -> new BadRequestException("Đơn hàng với ID " + shipmentId + " chưa được gán."));
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new BadRequestException("Tài xế với ID " + driverId + " không tồn tại."));
        if (previous.getDriver().getId().equals(driverId)) {
            throw new BadRequestException("Đơn hàng đã được phân công cho tài xế này.");
        }
        if (!driver.getStatus().equals(DriverStatus.AVAILABLE)) {
            throw new ConflictException("Tài xế với ID " + driverId + " không khả dụng.");
        }

        Shipment shipment = previous.getShipment();
        if (shipment.getStatus() == ShipmentStatus.PICKED_UP) {
            throw new ConflictException("Đơn hàng đã được lấy, không thể đổi tài xế.");
        }
        previous.setStatus(AssignmentStatus.CANCELLED);
        previous.setReason("Đổi tài xế");
        previous.setRespondedAt(Instant.now());
        assignmentRepository.save(previous);

        Assignment replacement = new Assignment();
        replacement.setShipment(shipment);
        replacement.setDriver(driver);
        replacement.setStatus(AssignmentStatus.OFFERED);
        replacement.setExpiresAt(Instant.now().plus(responseTimeoutMinutes, ChronoUnit.MINUTES));
        assignmentRepository.save(replacement);
        shipment.setCurrentDriver(driver);
        shipmentRepository.save(shipment);

        return toResponse(replacement);
    }

    @Override
    @Transactional
    public void revokeAssignment(Long shipmentId, User user, String reason) {
        Assignment assignment = assignmentRepository.findByShipmentIdAndStatusIn(shipmentId, ACTIVE_STATUSES)
                .orElseThrow(() -> new BadRequestException("Đơn hàng với ID " + shipmentId + " chưa được gán."));
        String shipmentStatus = assignment.getShipment().getStatus().toString();
        if (shipmentStatus.equals("PICKED_UP")) {
            throw new ConflictException("Đơn hàng với ID " + shipmentId + " đã được tài xế nhận, không thể thu hồi.");
        }

        assignment.setStatus(AssignmentStatus.CANCELLED);
        assignment.setReason(reason);
        assignment.setRespondedAt(Instant.now());
        assignmentRepository.save(assignment);

        assignment.getShipment().setCurrentDriver(null);
        shipmentRepository.save(assignment.getShipment());
        shipmentService.transitionStatus(shipmentId, ShipmentStatus.CREATED, user, reason);
    }

    @Override
    @Transactional
    public AssignmentResponse acceptAssignment(Long assignmentId, User user) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Phân công với ID " + assignmentId + " không tồn tại."));
        Driver driver = driverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ tài xế."));
        if (!assignment.getDriver().getId().equals(driver.getId())) {
            throw new BadRequestException(
                    "Phân công với ID " + assignmentId + " không được gán cho tài khoản của bạn.");
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

        return toResponse(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponse rejectAssignment(Long assignmentId, Long shipmentId, User user, String reason) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Phân công với ID " + assignmentId + " không tồn tại."));
        Driver driver = driverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ tài xế."));
        if (!assignment.getDriver().getId().equals(driver.getId())
                || !assignment.getShipment().getId().equals(shipmentId)) {
            throw new BadRequestException("Tài xế không được gán cho đơn hàng với ID " + shipmentId);
        }
        if (!assignment.getStatus().equals(AssignmentStatus.OFFERED)) {
            throw new ConflictException("Chỉ có thể từ chối lời mời đang chờ phản hồi.");
        }

        assignment.setReason(reason);
        assignment.setRespondedAt(Instant.now());
        assignment.setStatus(AssignmentStatus.REJECTED);
        assignmentRepository.save(assignment);

        assignment.getShipment().setCurrentDriver(null);
        shipmentRepository.save(assignment.getShipment());
        shipmentService.transitionStatus(shipmentId, ShipmentStatus.CREATED, user, reason);

        return toResponse(assignment);
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
            assignmentRepository.save(assignment);

            Shipment shipment = assignment.getShipment();
            shipment.setCurrentDriver(null);
            shipmentRepository.save(shipment);

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
    @Transactional
    public List<AssignmentResponse> getAssignmentsByShipment(Long shipmentId) {
        if (!shipmentRepository.existsById(shipmentId)) {
            throw new NotFoundException("Đơn hàng với ID " + shipmentId + " không tồn tại.");
        }
        return assignmentRepository.findAllByShipmentIdOrderByOfferedAtDesc(shipmentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public Page<AssignmentResponse> getDriverAssignments(Long id, AssignmentStatus status, Pageable pageable) {
        Driver driver = driverRepository.findByUserId(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ tài xế."));
        Page<Assignment> assignments = status == null
                ? assignmentRepository.findByDriverId(driver.getId(), pageable)
                : assignmentRepository.findByDriverIdAndStatus(driver.getId(), status, pageable);
        return assignments.map(this::toResponse);
    }

    @Override
    @Transactional
    public AssignmentResponse getAssignmentById(Long assignmentId, User user) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Phân công với ID " + assignmentId + " không tồn tại."));
        if (user.getRole() != com.trgmson.backend.model.enums.Role.ADMIN) {
            Driver driver = driverRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ tài xế."));
            if (!assignment.getDriver().getId().equals(driver.getId())) {
                throw new BadRequestException("Bạn không có quyền xem lượt phân công này.");
            }
        }
        return toResponse(assignment);
    }

    private AssignmentResponse toResponse(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getShipment().getId(),
                assignment.getDriver() == null ? null : assignment.getDriver().getId(),
                assignment.getStatus().name());
    }
}
