import { useEffect, useState } from 'react'
import { getMyAssignments, getShipmentById, respondToAssignment } from '../../api'
import { formatDate, formatMoney } from '../../utils/formatters'
import Icon from '../ui/Icon'

const ASSIGNMENT_LABELS = {
  OFFERED: 'Chờ phản hồi',
  ACCEPTED: 'Đã chấp nhận',
  REJECTED: 'Đã từ chối',
  EXPIRED: 'Đã hết hạn',
  CANCELLED: 'Đã thu hồi',
}

const FILTERS = [
  ['', 'Tất cả'],
  ['OFFERED', 'Chờ phản hồi'],
  ['ACCEPTED', 'Đã chấp nhận'],
  ['REJECTED', 'Đã từ chối'],
  ['EXPIRED', 'Đã hết hạn'],
  ['CANCELLED', 'Đã thu hồi'],
]

export default function DriverAssignments() {
  const [status, setStatus] = useState('OFFERED')
  const [page, setPage] = useState(0)
  const [pageData, setPageData] = useState(null)
  const [items, setItems] = useState([])
  const [reasons, setReasons] = useState({})
  const [loading, setLoading] = useState(true)
  const [busyId, setBusyId] = useState(null)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let active = true
    getMyAssignments({ page, status })
      .then(async (result) => {
        const hydrated = await Promise.all(result.content.map(async (assignment) => ({
          assignment,
          shipment: await getShipmentById(assignment.shipmentId),
        })))
        if (active) {
          setPageData(result)
          setItems(hydrated)
          setError('')
        }
      })
      .catch((loadError) => {
        if (active) setError(loadError.message)
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => { active = false }
  }, [page, status, reloadKey])

  async function handleResponse(assignment, action) {
    const reason = reasons[assignment.id]?.trim() || ''
    if (action === 'reject' && !reason) {
      setError('Vui lòng nhập lý do từ chối.')
      return
    }

    setBusyId(assignment.id)
    setError('')
    setLoading(true)
    try {
      await respondToAssignment(assignment.id, action, assignment.shipmentId, reason)
      setReloadKey((key) => key + 1)
    } catch (actionError) {
      setError(actionError.message)
    } finally {
      setBusyId(null)
    }
  }

  return (
    <section className="workflow-page">
      <header className="workflow-heading">
        <div>
          <span className="eyebrow eyebrow-green">CÔNG VIỆC CỦA TÔI</span>
          <h1>Lời mời giao hàng</h1>
          <p>Xem các lượt phân công và phản hồi lời mời đang chờ.</p>
        </div>
        <select
          className="filter-select assignment-filter"
          aria-label="Lọc trạng thái phân công"
          value={status}
          onChange={(event) => { setLoading(true); setStatus(event.target.value); setPage(0) }}
        >
          {FILTERS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
        </select>
      </header>

      {error && <div className="error-banner workflow-error" role="alert"><span>{error}</span><button type="button" onClick={() => setReloadKey((key) => key + 1)}>Thử lại</button></div>}

      {loading ? (
        <div className="workflow-loading"><span className="spinner" />Đang tải phân công…</div>
      ) : items.length ? (
        <div className="assignment-list">
          {items.map(({ assignment, shipment }) => (
            <article className="assignment-card" key={assignment.id}>
              <div className="assignment-card-top">
                <span className="tracking-code">{shipment.trackingCode}</span>
                <span className={`assignment-status assignment-${assignment.status.toLowerCase()}`}>
                  <i />{ASSIGNMENT_LABELS[assignment.status] || assignment.status}
                </span>
              </div>
              <div className="assignment-route">
                <div><small>NGƯỜI NHẬN</small><b>{shipment.receiverName}</b><span>{shipment.receiverPhone}</span></div>
                <div className="assignment-address"><Icon name="pin" size={16} /><span>{shipment.deliveryAddress}</span></div>
              </div>
              <div className="assignment-card-bottom">
                <span className="assignment-meta"><Icon name="clock" size={15} /> {formatDate(shipment.createdAt)}</span>
                <span className="assignment-cod">COD <b>{formatMoney(shipment.codAmount)}</b></span>
              </div>
              {assignment.status === 'OFFERED' && (
                <div className="assignment-response">
                  <input
                    aria-label={`Lý do từ chối ${shipment.trackingCode}`}
                    placeholder="Lý do từ chối (bắt buộc nếu từ chối)"
                    value={reasons[assignment.id] || ''}
                    maxLength={500}
                    onChange={(event) => setReasons((current) => ({ ...current, [assignment.id]: event.target.value }))}
                  />
                  <button className="button button-quiet" type="button" disabled={busyId === assignment.id} onClick={() => handleResponse(assignment, 'reject')}>Từ chối</button>
                  <button className="button button-primary" type="button" disabled={busyId === assignment.id} onClick={() => handleResponse(assignment, 'accept')}>
                    {busyId === assignment.id ? 'Đang gửi…' : 'Chấp nhận'} <Icon name="check" size={16} />
                  </button>
                </div>
              )}
            </article>
          ))}
        </div>
      ) : (
        <div className="workflow-empty">
          <span className="empty-icon"><Icon name="box" size={24} /></span>
          <b>Không có lượt phân công</b>
          <p>Lời mời mới từ điều phối sẽ xuất hiện tại đây.</p>
        </div>
      )}

      <div className="table-footer assignment-pagination">
        <span>{pageData?.totalElements || 0} lượt phân công</span>
        <div className="pagination">
          <button className="button button-quiet" type="button" disabled={!page || loading} onClick={() => { setLoading(true); setPage((value) => value - 1) }}>Trước</button>
          <button className="button button-quiet" type="button" disabled={!pageData || page + 1 >= pageData.totalPages || loading} onClick={() => { setLoading(true); setPage((value) => value + 1) }}>Tiếp theo <Icon name="arrow" size={15} /></button>
        </div>
      </div>
    </section>
  )
}
