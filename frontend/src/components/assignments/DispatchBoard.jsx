import { useEffect, useState } from 'react'
import {
  assignDriver,
  getAssignmentHistory,
  getDrivers,
  getShipments,
  revokeAssignment,
} from '../../api'
import { STATUS_LABELS } from '../../constants/shipment'
import Icon from '../ui/Icon'

const ACTIVE_ASSIGNMENTS = ['OFFERED', 'ACCEPTED']

export default function DispatchBoard() {
  const [shipments, setShipments] = useState([])
  const [drivers, setDrivers] = useState([])
  const [histories, setHistories] = useState({})
  const [selectedDrivers, setSelectedDrivers] = useState({})
  const [revokeReasons, setRevokeReasons] = useState({})
  const [loading, setLoading] = useState(true)
  const [busyId, setBusyId] = useState(null)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let active = true
    Promise.all([
      getShipments({ page: 0, size: 100, status: 'CREATED' }),
      getShipments({ page: 0, size: 100, status: 'ASSIGNED' }),
      getDrivers(),
    ])
      .then(async ([created, assigned, driverList]) => {
        const allShipments = [...created.content, ...assigned.content]
        const historyEntries = await Promise.all(
          allShipments.map(async (shipment) => [shipment.id, await getAssignmentHistory(shipment.id)]),
        )
        if (active) {
          setShipments(allShipments)
          setDrivers(driverList)
          setHistories(Object.fromEntries(historyEntries))
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
  }, [reloadKey])

  async function handleAssign(shipment, currentAssignment) {
    const driverId = Number(selectedDrivers[shipment.id])
    if (!driverId) {
      setError('Chọn tài xế trước khi phân công.')
      return
    }
    setError('')
    setLoading(true)
    setBusyId(shipment.id)
    try {
      await assignDriver(shipment.id, driverId, Boolean(currentAssignment))
      setReloadKey((key) => key + 1)
    } catch (assignError) {
      setError(assignError.message)
    } finally {
      setBusyId(null)
    }
  }

  async function handleRevoke(shipment) {
    setError('')
    setLoading(true)
    setBusyId(shipment.id)
    try {
      await revokeAssignment(shipment.id, revokeReasons[shipment.id] || '')
      setReloadKey((key) => key + 1)
    } catch (revokeError) {
      setError(revokeError.message)
    } finally {
      setBusyId(null)
    }
  }

  function getActiveAssignment(shipment) {
    return (histories[shipment.id] || []).find((assignment) => ACTIVE_ASSIGNMENTS.includes(assignment.status))
  }

  return (
    <section className="workflow-page">
      <header className="workflow-heading">
        <div>
          <span className="eyebrow eyebrow-green">ĐIỀU PHỐI VẬN ĐƠN</span>
          <h1>Phân công tài xế</h1>
          <p>Gán vận đơn mới, đổi tài xế hoặc thu hồi lượt phân công.</p>
        </div>
        <div className="dispatch-summary">
          <span>{shipments.filter((shipment) => shipment.status === 'CREATED').length} chờ phân công</span>
          <span>{shipments.filter((shipment) => shipment.status === 'ASSIGNED').length} đã phân công</span>
        </div>
      </header>

      {error && <div className="error-banner workflow-error" role="alert"><span>{error}</span><button type="button" onClick={() => setReloadKey((key) => key + 1)}>Thử lại</button></div>}

      {loading ? (
        <div className="workflow-loading"><span className="spinner" />Đang tải vận đơn và tài xế…</div>
      ) : shipments.length ? (
        <div className="dispatch-list">
          {shipments.map((shipment) => {
            const assignment = getActiveAssignment(shipment)
            const assignedDriver = drivers.find((driver) => driver.id === assignment?.driverId)
            const availableDrivers = drivers.filter((driver) =>
              driver.status === 'AVAILABLE' && driver.id !== assignment?.driverId,
            )
            const canRevoke = assignment && shipment.status !== 'PICKED_UP'

            return (
              <article className="dispatch-card" key={shipment.id}>
                <div className="dispatch-card-main">
                  <div className="dispatch-card-title">
                    <span className="tracking-code">{shipment.trackingCode}</span>
                    <span className={`status-badge status-${shipment.status.toLowerCase()}`}><i />{STATUS_LABELS[shipment.status]}</span>
                  </div>
                  <div className="dispatch-recipient">
                    <b>{shipment.receiverName}</b><span>{shipment.receiverPhone}</span>
                  </div>
                  <div className="dispatch-address"><Icon name="pin" size={15} /><span>{shipment.deliveryAddress}</span></div>
                  {assignedDriver && <div className="dispatch-assignee">Đang chờ: <b>{assignedDriver.fullName}</b> · {assignment.status === 'ACCEPTED' ? 'Đã nhận đơn' : 'Chờ tài xế phản hồi'}</div>}
                </div>
                <div className="dispatch-controls">
                  <label>
                    <span>{assignment ? 'Đổi tài xế' : 'Chọn tài xế'}</span>
                    <select
                      aria-label={`Tài xế cho ${shipment.trackingCode}`}
                      value={selectedDrivers[shipment.id] || ''}
                      onChange={(event) => setSelectedDrivers((current) => ({ ...current, [shipment.id]: event.target.value }))}
                    >
                      <option value="">Chọn tài xế khả dụng</option>
                      {availableDrivers.map((driver) => (
                        <option key={driver.id} value={driver.id}>{driver.fullName} · {driver.serviceArea || driver.licensePlate || driver.email}</option>
                      ))}
                    </select>
                  </label>
                  <button className="button button-primary dispatch-action" type="button" disabled={busyId === shipment.id || !selectedDrivers[shipment.id]} onClick={() => handleAssign(shipment, assignment)}>
                    {busyId === shipment.id ? 'Đang lưu…' : assignment ? 'Đổi tài xế' : 'Phân công'}
                  </button>
                  {canRevoke && (
                    <div className="dispatch-revoke">
                      <input
                        aria-label={`Lý do thu hồi ${shipment.trackingCode}`}
                        placeholder="Lý do thu hồi (không bắt buộc)"
                        maxLength={500}
                        value={revokeReasons[shipment.id] || ''}
                        onChange={(event) => setRevokeReasons((current) => ({ ...current, [shipment.id]: event.target.value }))}
                      />
                      <button className="button button-quiet" type="button" disabled={busyId === shipment.id} onClick={() => handleRevoke(shipment)}>Thu hồi</button>
                    </div>
                  )}
                  {!availableDrivers.length && !assignment && <small className="no-drivers">Hiện không có tài xế khả dụng.</small>}
                </div>
                {(histories[shipment.id] || []).length > 0 && (
                  <details className="assignment-history">
                    <summary>Lịch sử phân công ({histories[shipment.id].length})</summary>
                    <div className="history-items">
                      {histories[shipment.id].map((item) => {
                        const driver = drivers.find((entry) => entry.id === item.driverId)
                        return <span key={item.id}>#{item.id} · {driver?.fullName || (item.driverId ? `Tài xế #${item.driverId}` : 'Chưa xác định tài xế')} · {item.status}</span>
                      })}
                    </div>
                  </details>
                )}
              </article>
            )
          })}
        </div>
      ) : (
        <div className="workflow-empty">
          <span className="empty-icon"><Icon name="check" size={24} /></span>
          <b>Không có đơn cần điều phối</b>
          <p>Vận đơn ở trạng thái chờ xử lý hoặc đã phân công sẽ xuất hiện tại đây.</p>
        </div>
      )}
    </section>
  )
}
