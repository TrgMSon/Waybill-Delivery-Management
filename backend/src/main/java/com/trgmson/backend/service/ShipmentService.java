package com.trgmson.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.trgmson.backend.dto.shipment.CreateShipmentRequest;
import com.trgmson.backend.dto.shipment.ShipmentResponse;
import com.trgmson.backend.dto.shipment.UpdateShipmentRequest;
import com.trgmson.backend.model.User;
import com.trgmson.backend.model.enums.ShipmentStatus;

public interface ShipmentService {
    // 1. Tạo vận đơn
    ShipmentResponse createShipment(CreateShipmentRequest req, User creator);

    // 2. Đọc vận đơn
    ShipmentResponse getById(Long id, User currentUser);
    Page<ShipmentResponse> getShipments(ShipmentStatus status, Pageable pageable, User currentUser);

    // 3. Sửa thông tin khi còn CREATED
    ShipmentResponse updateShipment(Long id, UpdateShipmentRequest req);

    // 4. Phương thức trung tâm duy nhất cho mọi lần đổi trạng thái
    ShipmentResponse transitionStatus(Long shipmentId, ShipmentStatus nextStatus, User actor, String note);
}
