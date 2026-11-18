package com.trgmson.backend.service.impl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trgmson.backend.dto.shipment.CreateShipmentRequest;
import com.trgmson.backend.dto.shipment.ShipmentResponse;
import com.trgmson.backend.dto.shipment.UpdateShipmentRequest;
import com.trgmson.backend.exception.IllegalArgumentException;
import com.trgmson.backend.exception.ConflictException;
import com.trgmson.backend.exception.NotFoundException;
import com.trgmson.backend.model.Shipment;
import com.trgmson.backend.model.ShipmentStatusHistory;
import com.trgmson.backend.model.User;
import com.trgmson.backend.model.enums.Role;
import com.trgmson.backend.model.enums.ShipmentStatus;
import com.trgmson.backend.repository.ShipmentRepository;
import com.trgmson.backend.repository.ShipmentStatusHistoryRepository;
import com.trgmson.backend.repository.AssignmentRepository;
import com.trgmson.backend.service.ShipmentService;

@Service
public class ShipmentServiceImpl implements ShipmentService {

    private static final String TRACKING_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Bỏ 0, O, 1, I tránh nhầm lẫn
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyMMdd");

    private final ShipmentRepository shipmentRepository;
    private final ShipmentStatusHistoryRepository historyRepository;
    private final AssignmentRepository assignmentRepository;

    public ShipmentServiceImpl(ShipmentRepository shipmentRepository,
                               ShipmentStatusHistoryRepository historyRepository,
                               AssignmentRepository assignmentRepository) {
        this.shipmentRepository = shipmentRepository;
        this.historyRepository = historyRepository;
        this.assignmentRepository = assignmentRepository;
    }

    // =========================================================================
    // 1. TẠO VẬN ĐƠN
    // =========================================================================
    @Override
    @Transactional
    public ShipmentResponse createShipment(CreateShipmentRequest req, User creator) {
        String trackingCode = generateUniqueTrackingCode();

        Shipment shipment = Shipment.builder()
                .trackingCode(trackingCode)
                .senderName(req.getSenderName())
                .senderPhone(req.getSenderPhone())
                .pickupAddress(req.getPickupAddress())
                .receiverName(req.getReceiverName())
                .receiverPhone(req.getReceiverPhone())
                .deliveryAddress(req.getDeliveryAddress())
                .codAmount(req.getCodAmount())
                .shippingFee(req.getShippingFee())
                .status(ShipmentStatus.CREATED)
                .createdBy(creator)
                .build();

        Shipment saved = shipmentRepository.save(shipment);

        // Ghi mốc lịch sử khởi tạo đầu tiên
        saveHistory(saved, null, ShipmentStatus.CREATED, creator);

        return mapToResponse(saved);
    }

    // =========================================================================
    // 2. ĐỌC VẬN ĐƠN (THEO ID & DANH SÁCH PHÂN TRANG / LỌC TRẠNG THÁI)
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getById(Long id, User currentUser) {
        if (!assignmentRepository.existsByShipmentIdAndUserId(id, currentUser.getId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new ConflictException("Người dùng không có quyền truy cập vận đơn này");
        }

        Shipment shipment = findShipmentOrThrow(id);
        return mapToResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getShipments(ShipmentStatus status, Pageable pageable, User currentUser) {
        if (currentUser.getRole().equals(Role.ADMIN)) {
            Page<Shipment> page = (status == null)
                    ? shipmentRepository.findAll(pageable)
                    : shipmentRepository.findByStatus(status, pageable);
            return page.map(this::mapToResponse);
        }
        // Nếu là DRIVER, chỉ lấy các vận đơn được phân công cho họ
        if (currentUser.getRole().equals(Role.DRIVER)) {
            Page<Shipment> page = (status == null)
                    ? shipmentRepository.findByAssignedDriverId(currentUser.getId(), pageable)
                    : shipmentRepository.findByAssignedDriverIdAndStatus(currentUser.getId(), status, pageable);
            return page.map(this::mapToResponse);
        }

        throw new ConflictException("Người dùng không có quyền truy cập danh sách vận đơn này");
    }

    // =========================================================================
    // 3. SỬA THÔNG TIN KHI CÒN CREATED
    // =========================================================================
    @Override
    @Transactional
    public ShipmentResponse updateShipment(Long id, UpdateShipmentRequest req) {
        Shipment shipment = findShipmentOrThrow(id);

        if (shipment.getStatus() != ShipmentStatus.CREATED) {
            throw new ConflictException(
                    "Chỉ được phép chỉnh sửa thông tin khi vận đơn ở trạng thái CREATED. Trạng thái hiện tại: "
                            + shipment.getStatus());
        }

        shipment.setSenderName(req.getSenderName());
        shipment.setSenderPhone(req.getSenderPhone());
        shipment.setPickupAddress(req.getPickupAddress());
        shipment.setReceiverName(req.getReceiverName());
        shipment.setReceiverPhone(req.getReceiverPhone());
        shipment.setDeliveryAddress(req.getDeliveryAddress());
        shipment.setCodAmount(req.getCodAmount());
        shipment.setShippingFee(req.getShippingFee());

        return mapToResponse(shipmentRepository.save(shipment));
    }

    // =========================================================================
    // 4. PHƯƠNG THỨC TRUNG TÂM DUY NHẤT CHO MỌI LẦN ĐỔI TRẠNG THÁI
    // =========================================================================
    @Override
    @Transactional
    public ShipmentResponse transitionStatus(Long shipmentId, ShipmentStatus nextStatus, User actor, String note) {
        Shipment shipment = findShipmentOrThrow(shipmentId);
        ShipmentStatus currentStatus = shipment.getStatus();

        // 1. Kiểm tra bước chuyển hợp lệ qua State Machine
        if (!currentStatus.canTransitionTo(nextStatus)) {
            throw new IllegalArgumentException(
                    String.format("Không thể chuyển trạng thái vận đơn từ %s sang %s", currentStatus, nextStatus));
        }

        // 2. Cập nhật trạng thái
        shipment.setStatus(nextStatus);

        Shipment updated = shipmentRepository.save(shipment);

        // 3. Ghi lịch sử biến động trạng thái
        saveHistory(updated, currentStatus, nextStatus, actor);

        return mapToResponse(updated);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================
    private Shipment findShipmentOrThrow(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy vận đơn với ID: " + id));
    }

    private void saveHistory(Shipment shipment, ShipmentStatus fromStatus, ShipmentStatus toStatus, User actor) {
        ShipmentStatusHistory history = ShipmentStatusHistory.builder()
                .shipment(shipment)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(actor)
                .build();
        historyRepository.save(history);
    }

    private String generateUniqueTrackingCode() {
        String datePart = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(DATE_FMT);
        for (int attempt = 0; attempt < 5; attempt++) {
            StringBuilder sb = new StringBuilder("TMS").append(datePart);
            for (int i = 0; i < 6; i++) {
                sb.append(TRACKING_CHARS.charAt(RANDOM.nextInt(TRACKING_CHARS.length())));
            }
            String candidate = sb.toString(); // VD: TMS261117X9K2P4
            if (!shipmentRepository.existsByTrackingCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Không thể sinh mã tracking_code duy nhất sau nhiều lần thử");
    }

    private ShipmentResponse mapToResponse(Shipment s) {
        return ShipmentResponse.builder()
                .id(s.getId())
                .trackingCode(s.getTrackingCode())
                .senderName(s.getSenderName())
                .senderPhone(s.getSenderPhone())
                .pickupAddress(s.getPickupAddress())
                .receiverName(s.getReceiverName())
                .receiverPhone(s.getReceiverPhone())
                .deliveryAddress(s.getDeliveryAddress())
                .codAmount(s.getCodAmount())
                .shippingFee(s.getShippingFee())
                .status(s.getStatus().name())
                .createdAt(s.getCreatedAt())
                .build();
    }
}