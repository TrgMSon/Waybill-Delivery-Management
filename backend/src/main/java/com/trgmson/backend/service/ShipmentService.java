package com.trgmson.backend.service;

import com.trgmson.backend.model.enums.ShipmentStatus;

public class ShipmentService {
    // 1. Tạo vận đơn
    ShipmentResponse createShipment(CreateShipmentRequest req, User creator);

    // 2. Đọc vận đơn
    ShipmentResponse getById(UUID id);
    Page<ShipmentResponse> getShipments(ShipmentStatus status, Pageable pageable);

    // 3. Sửa thông tin khi còn CREATED
    ShipmentResponse updateShipment(UUID id, UpdateShipmentRequest req);

    // 4. Phương thức trung tâm duy nhất cho mọi lần đổi trạng thái
    ShipmentResponse transitionStatus(UUID shipmentId, ShipmentStatus nextStatus, User actor, String note);
}
