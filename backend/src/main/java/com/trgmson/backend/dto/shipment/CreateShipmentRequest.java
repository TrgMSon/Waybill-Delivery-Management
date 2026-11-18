package com.trgmson.backend.dto.shipment;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentRequest {

    @NotBlank(message = "Tên người gửi không được để trống")
    @Size(max = 100, message = "Tên người gửi tối đa 100 ký tự")
    private String senderName;

    @NotBlank(message = "Số điện thoại người gửi không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại người gửi không hợp lệ")
    private String senderPhone;

    @NotBlank(message = "Địa chỉ lấy hàng không được để trống")
    @Size(max = 255, message = "Địa chỉ lấy hàng tối đa 255 ký tự")
    private String pickupAddress;

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự")
    private String receiverName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại người nhận không hợp lệ")
    private String receiverPhone;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 255, message = "Địa chỉ giao hàng tối đa 255 ký tự")
    private String deliveryAddress;

    @NotNull(message = "Tiền thu hộ (COD) không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tiền COD phải lớn hơn hoặc bằng 0")
    private BigDecimal codAmount;

    @NotNull(message = "Phí vận chuyển không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Phí vận chuyển phải lớn hơn hoặc bằng 0")
    private BigDecimal shippingFee;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}