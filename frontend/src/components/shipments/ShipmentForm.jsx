import { useState } from 'react';
import { createShipment } from '../../api';
import Icon from '../ui/Icon';
import Modal from '../ui/Modal';

export default function ShipmentForm({ onClose, onCreated }) {
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSaving(true);
    const formData = new FormData(event.currentTarget);
    const shipment = {
      senderName: formData.get('senderName'),
      senderPhone: formData.get('senderPhone'),
      pickupAddress: formData.get('pickupAddress'),
      receiverName: formData.get('receiverName'),
      receiverPhone: formData.get('receiverPhone'),
      deliveryAddress: formData.get('deliveryAddress'),
      codAmount: Number(formData.get('codAmount')),
      shippingFee: Number(formData.get('shippingFee')),
      note: formData.get('note'),
    };

    try {
      await createShipment(shipment);
      onCreated();
    } catch (createError) {
      setError(createError.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal title="Tạo vận đơn mới" onClose={onClose} subtitle="Nhập thông tin gửi hàng để khởi tạo đơn.">
      <form className="shipment-form" onSubmit={handleSubmit}>
        <div className="form-section-title">Thông tin người gửi</div>
        <div className="form-grid">
          <Field label="Tên người gửi" name="senderName" placeholder="Nguyễn Văn A" />
          <Field label="Số điện thoại" name="senderPhone" placeholder="0901234567" />
          <Field className="field-full" label="Địa chỉ lấy hàng" name="pickupAddress" placeholder="Số nhà, đường, quận/huyện, tỉnh/thành" />
        </div>
        <div className="form-section-title form-section-spaced">Thông tin người nhận</div>
        <div className="form-grid">
          <Field label="Tên người nhận" name="receiverName" placeholder="Trần Thị B" />
          <Field label="Số điện thoại" name="receiverPhone" placeholder="0901234567" />
          <Field className="field-full" label="Địa chỉ giao hàng" name="deliveryAddress" placeholder="Số nhà, đường, quận/huyện, tỉnh/thành" />
        </div>
        <div className="form-section-title form-section-spaced">Thanh toán</div>
        <div className="form-grid">
          <Field label="Tiền thu hộ (COD)" name="codAmount" type="number" min="0" step="1000" defaultValue="0" suffix="₫" />
          <Field label="Phí vận chuyển" name="shippingFee" type="number" min="0" step="1000" defaultValue="30000" suffix="₫" />
          <Field className="field-full" label="Ghi chú" name="note" placeholder="Lưu ý khi giao hàng (không bắt buộc)" required={false} />
        </div>
        {error && <p className="form-error" role="alert">{error}</p>}
        <div className="modal-actions">
          <button className="button button-quiet" type="button" onClick={onClose}>Hủy</button>
          <button className="button button-primary" type="submit" disabled={saving}>{saving ? 'Đang tạo…' : 'Tạo vận đơn'} <Icon name="arrow" size={17} /></button>
        </div>
      </form>
    </Modal>
  )
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
