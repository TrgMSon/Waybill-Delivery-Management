package com.trgmson.backend.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trgmson.backend.common.ApiResponse;
import com.trgmson.backend.dto.assignment.AssignDriverRequest;
import com.trgmson.backend.dto.assignment.AssignmentResponse;
import com.trgmson.backend.dto.assignment.RejectAssignmentRequest;
import com.trgmson.backend.dto.assignment.RevokeAssignmentRequest;
import com.trgmson.backend.model.User;
import com.trgmson.backend.model.enums.AssignmentStatus;
import com.trgmson.backend.service.AssignmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    // ==================== ADMIN / DISPATCHER ====================

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> assignDriver(
            @Valid @RequestBody AssignDriverRequest request,
            @AuthenticationPrincipal User currentUser) {

        AssignmentResponse response = assignmentService.assignShipmentToDriver(
                request.getShipmentId(),
                request.getDriverId(),
                currentUser
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success( "Gán tài xế cho vận đơn thành công", response));
    }

    @PostMapping("/reassign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> reassignDriver(
            @Valid @RequestBody AssignDriverRequest request,
            @AuthenticationPrincipal User currentUser) {

        AssignmentResponse response = assignmentService.reassignShipmentToDriver(
                request.getShipmentId(),
                request.getDriverId(),
                currentUser
        );
        return ResponseEntity.ok(ApiResponse.success("Đổi tài xế cho vận đơn thành công", response));
    }

    @PostMapping("/shipments/{shipmentId}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revokeAssignment(
            @PathVariable Long shipmentId,
            @RequestBody(required = false) RevokeAssignmentRequest request,
            @AuthenticationPrincipal User currentUser) {

        String reason = (request != null && request.getReason() != null && !request.getReason().isBlank())
                ? request.getReason().trim()
                : "Admin thu hồi lượt phân công";

        assignmentService.revokeAssignment(shipmentId, currentUser, reason);
        return ResponseEntity.ok(ApiResponse.success("Thu hồi phân công thành công", null));
    }

    @GetMapping("/shipments/{shipmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getAssignmentsByShipment(
            @PathVariable Long shipmentId) {

        List<AssignmentResponse> history = assignmentService.getAssignmentsByShipment(shipmentId);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử phân công thành công", history));
    }

    // ==================== DRIVER ====================

    @GetMapping("/my")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<Page<AssignmentResponse>>> getMyAssignments(
            @RequestParam(required = false) AssignmentStatus status,
            @PageableDefault(size = 10, sort = "offeredAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User currentDriver) {

        Page<AssignmentResponse> page = assignmentService.getDriverAssignments(
                currentDriver.getId(),
                status,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn phân công thành công", page));
    }

    @PostMapping("/{assignmentId}/accept")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> acceptAssignment(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User currentDriver) {

        AssignmentResponse response = assignmentService.acceptAssignment(assignmentId, currentDriver);
        return ResponseEntity.ok(ApiResponse.success("Đã chấp nhận vận đơn", response));
    }

    @PostMapping("/{assignmentId}/reject")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> rejectAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody RejectAssignmentRequest request,
            @AuthenticationPrincipal User currentDriver) {

        AssignmentResponse response = assignmentService.rejectAssignment(
                assignmentId,
                request.getShipmentId(),
                currentDriver,
                request.getReason().trim()
        );
        return ResponseEntity.ok(ApiResponse.success("Đã từ chối vận đơn", response));
    }

    // ==================== SHARED DETAIL ====================

    @GetMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignmentById(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User currentUser) {

        AssignmentResponse response = assignmentService.getAssignmentById(assignmentId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết lượt gán thành công", response));
    }
}