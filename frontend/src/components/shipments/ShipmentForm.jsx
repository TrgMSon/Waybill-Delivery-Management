import { useState } from 'react';
import { createShipment, updateShipment } from '../../api'
import Icon from '../ui/Icon';
import Modal from '../ui/Modal';

export default function ShipmentForm({ shipment, onClose, onCreated }) {
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSaving(true);
    const formData = new FormData(event.currentTarget);
    const payload = formDataToShipment(formData)

    try {
      if (shipment?.id) {
        await updateShipment(shipment.id, payload)
      } else {
        await createShipment(payload)
      }
      onCreated();
    } catch (createError) {
      setError(createError.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal title={shipment ? 'Chỉnh sửa vận đơn' : 'Tạo vận đơn mới'} onClose={onClose} subtitle={shipment ? `Cập nhật thông tin ${shipment.trackingCode}.` : 'Nhập thông tin gửi hàng để khởi tạo đơn.'}>
      <form className="shipment-form" onSubmit={handleSubmit}>
        <div className="form-section-title">Thông tin người gửi</div>
        <div className="form-grid">
          <Field label="Tên người gửi" name="senderName" placeholder="Nguyễn Văn A" defaultValue={shipment?.senderName} />
          <Field label="Số điện thoại" name="senderPhone" placeholder="0901234567" defaultValue={shipment?.senderPhone} />
          <Field className="field-full" label="Địa chỉ lấy hàng" name="pickupAddress" placeholder="Số nhà, đường, quận/huyện, tỉnh/thành" defaultValue={shipment?.pickupAddress} />
        </div>
        <div className="form-section-title form-section-spaced">Thông tin người nhận</div>
        <div className="form-grid">
          <Field label="Tên người nhận" name="receiverName" placeholder="Trần Thị B" defaultValue={shipment?.receiverName} />
          <Field label="Số điện thoại" name="receiverPhone" placeholder="0901234567" defaultValue={shipment?.receiverPhone} />
          <Field className="field-full" label="Địa chỉ giao hàng" name="deliveryAddress" placeholder="Số nhà, đường, quận/huyện, tỉnh/thành" defaultValue={shipment?.deliveryAddress} />
        </div>
        <div className="form-section-title form-section-spaced">Thanh toán</div>
        <div className="form-grid">
          <Field label="Khối lượng" name="weightKg" type="number" min="0.01" step="0.01" defaultValue={shipment?.weightKg || '1'} suffix="kg" />
          <Field label="Tiền thu hộ (COD)" name="codAmount" type="number" min="0" step="1000" defaultValue={shipment?.codAmount ?? '0'} suffix="₫" />
          <Field label="Phí vận chuyển" name="shippingFee" type="number" min="0" step="1000" defaultValue={shipment?.shippingFee ?? '30000'} suffix="₫" />
        </div>
        {error && <p className="form-error" role="alert">{error}</p>}
        <div className="modal-actions">
          <button className="button button-quiet" type="button" onClick={onClose}>Hủy</button>
          <button className="button button-primary" type="submit" disabled={saving}>{saving ? 'Đang lưu…' : shipment ? 'Lưu thay đổi' : 'Tạo vận đơn'} <Icon name="arrow" size={17} /></button>
        </div>
      </form>
    </Modal>
  )
}

function formDataToShipment(formData) {
  return {
    senderName: formData.get('senderName'),
    senderPhone: formData.get('senderPhone'),
    pickupAddress: formData.get('pickupAddress'),
    receiverName: formData.get('receiverName'),
    receiverPhone: formData.get('receiverPhone'),
    deliveryAddress: formData.get('deliveryAddress'),
    weightKg: Number(formData.get('weightKg')),
    codAmount: Number(formData.get('codAmount')),
    shippingFee: Number(formData.get('shippingFee')),
  }
}

function Field({ label, name, className = '', type = 'text', placeholder, min, step, defaultValue, suffix, required = true }) {
  return (
    <label className={`form-field ${className}`} htmlFor={name}>
      <span>{label}</span>
      <div className="input-with-suffix">
        <input id={name} name={name} type={type} placeholder={placeholder} min={min} step={step} defaultValue={defaultValue} required={required} />
        {suffix && <span className="input-suffix">{suffix}</span>}
      </div>
    </label>
  )
}
