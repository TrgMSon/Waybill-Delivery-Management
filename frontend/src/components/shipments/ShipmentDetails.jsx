import { useState } from 'react';
import { transitionShipment } from '../../api';
import { STATUS_LABELS, STATUS_TRANSITIONS } from '../../constants/shipment';
import { formatDate, formatMoney } from '../../utils/formatters';
import Icon from '../ui/Icon';
import Modal from '../ui/Modal';

export default function ShipmentDetails({ shipment, role, onClose, onUpdated }) {
  const [nextStatus, setNextStatus] = useState('');
  const [note, setNote] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const options = STATUS_TRANSITIONS[shipment.status] || [];

  async function handleStatusChange(event) {
    event.preventDefault();
    if (!nextStatus) return;
    setError('');
    setSaving(true);
    try {
      const updated = await transitionShipment(shipment.id, nextStatus, note);
      onUpdated(updated);
    } catch (updateError) {
      setError(updateError.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal title="Chi tiết vận đơn" subtitle={`Mã vận đơn ${shipment.trackingCode}`} onClose={onClose} wide>
      <div className="shipment-detail">
        <div className="detail-summary">
          <span className={`status-badge status-${shipment.status?.toLowerCase()}`}><i />{STATUS_LABELS[shipment.status] || shipment.status}</span>
          <span className="detail-created"><Icon name="clock" size={15} /> Tạo lúc {formatDate(shipment.createdAt)}</span>
        </div>
        <div className="detail-route">
          <DetailAddress label="ĐIỂM LẤY HÀNG" name={shipment.senderName} phone={shipment.senderPhone} address={shipment.pickupAddress} />
          <div className="route-arrow"><Icon name="arrow" size={20} /></div>
          <DetailAddress label="ĐIỂM GIAO HÀNG" name={shipment.receiverName} phone={shipment.receiverPhone} address={shipment.deliveryAddress} />
        </div>
        <div className="detail-payment">
          <div><span>Tiền thu hộ (COD)</span><b>{formatMoney(shipment.codAmount)}</b></div>
          <div><span>Phí vận chuyển</span><b>{formatMoney(shipment.shippingFee)}</b></div>
          {shipment.note && <div className="detail-note"><span>Ghi chú</span><b>{shipment.note}</b></div>}
        </div>
        {role === 'ADMIN' && options.length > 0 && (
          <form className="status-update" onSubmit={handleStatusChange}>
            <div className="form-section-title">Cập nhật trạng thái</div>
            <div className="status-update-controls">
              <select aria-label="Trạng thái mới" value={nextStatus} onChange={(event) => setNextStatus(event.target.value)} required>
                <option value="">Chọn trạng thái tiếp theo</option>
                {options.map((status) => <option key={status} value={status}>{STATUS_LABELS[status]}</option>)}
              </select>
              <input aria-label="Ghi chú trạng thái" placeholder="Ghi chú (không bắt buộc)" value={note} onChange={(event) => setNote(event.target.value)} maxLength={500} />
              <button className="button button-primary" type="submit" disabled={saving || !nextStatus}>{saving ? 'Đang lưu…' : 'Cập nhật'}</button>
            </div>
            {error && <p className="form-error" role="alert">{error}</p>}
          </form>
        )}
      </div>
    </Modal>
  )
}

function DetailAddress({ label, name, phone, address }) {
  return (
    <div className="detail-address">
      <span className="eyebrow">{label}</span>
      <b>{name}</b>
      <span>{phone}</span>
      <p><Icon name="pin" size={15} />{address}</p>
    </div>
  )
}
