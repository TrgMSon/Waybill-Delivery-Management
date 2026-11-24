package com.trgmson.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trgmson.backend.dto.shipment.CreateShipmentRequest;
import com.trgmson.backend.dto.shipment.ShipmentResponse;
import com.trgmson.backend.dto.shipment.TransitionStatusRequest;
import com.trgmson.backend.dto.shipment.UpdateShipmentRequest;
import com.trgmson.backend.model.User;
import com.trgmson.backend.model.enums.ShipmentStatus;
import com.trgmson.backend.service.ShipmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {
    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    // 1. Tạo vận đơn mới (Admin)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShipmentResponse> createShipment(
            @Valid @RequestBody CreateShipmentRequest request,
            @AuthenticationPrincipal User currentUser) {
        ShipmentResponse response = shipmentService.createShipment(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2a. Lấy chi tiết 1 vận đơn theo ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<ShipmentResponse> getShipmentById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(shipmentService.getById(id, currentUser));
    }

    // 2b. Danh sách vận đơn có phân trang và lọc theo trạng thái
    // VD: GET /api/shipments?status=CREATED&page=0&size=10&sort=createdAt,desc
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<Page<ShipmentResponse>> getShipments(
            @RequestParam(required = false) ShipmentStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(shipmentService.getShipments(status, pageable, currentUser));
    }

    // 3. Cập nhật thông tin vận đơn (chỉ cho phép khi trạng thái là CREATED)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShipmentResponse> updateShipment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShipmentRequest request) {
        return ResponseEntity.ok(shipmentService.updateShipment(id, request));
    }

    // 4. Đổi trạng thái vận đơn qua State Machine trung tâm
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShipmentResponse> transitionStatus(
            @PathVariable Long id,
            @Valid @RequestBody TransitionStatusRequest request,
            @AuthenticationPrincipal User currentUser) {
        ShipmentResponse response = shipmentService.transitionStatus(
                id,
                request.getNextStatus(),
                currentUser,
                request.getNote());
        return ResponseEntity.ok(response);
    }
}