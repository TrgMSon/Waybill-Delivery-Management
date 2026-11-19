import { useEffect, useState } from 'react'
import { getShipments } from '../../api'
import { STATUS_LABELS, STATUS_OPTIONS } from '../../constants/shipment'
import { formatDate, formatMoney } from '../../utils/formatters'
import Icon from '../ui/Icon'
import MetricCard from './MetricCard'
import ShipmentDetails from '../shipments/ShipmentDetails'
import ShipmentForm from '../shipments/ShipmentForm'

const CURRENT_YEAR = new Date().getFullYear()

export default function Dashboard({ user, onSignOut }) {
  const [page, setPage] = useState(0)
  const [status, setStatus] = useState('')
  const [search, setSearch] = useState('')
  const [pageData, setPageData] = useState(null)
  const [selected, setSelected] = useState(null)
  const [showCreate, setShowCreate] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [signingOut, setSigningOut] = useState(false)
  const [mobileMenu, setMobileMenu] = useState(false)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let active = true
    getShipments({ page, status })
      .then((result) => {
        if (active) {
          setPageData(result)
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

  const shipments = pageData?.content || []
  const query = search.trim().toLocaleLowerCase('vi')
  const visibleShipments = query
    ? shipments.filter((shipment) =>
        [shipment.trackingCode, shipment.receiverName, shipment.receiverPhone, shipment.deliveryAddress]
          .some((value) => value?.toLocaleLowerCase('vi').includes(query)),
      )
    : shipments
  const totalPages = pageData?.totalPages || 0
  const totalItems = pageData?.totalElements || 0
  const activeItems = shipments.filter((item) => !['DELIVERED', 'CANCELLED', 'RETURNED'].includes(item.status)).length
  const deliveredItems = shipments.filter((item) => item.status === 'DELIVERED').length

  async function handleSignOut() {
    setSigningOut(true)
    try {
      await onSignOut()
    } catch (signOutError) {
      setError(signOutError.message)
    } finally {
      setSigningOut(false)
    }
  }

  function handleCreated() {
    setShowCreate(false)
    setLoading(true)
    setPage(0)
    if (page === 0) setReloadKey((key) => key + 1)
  }

  function handleUpdated(updated) {
    setSelected(updated)
    setLoading(true)
    setReloadKey((key) => key + 1)
  }

  return (
    <div className="app-shell">
      <aside className={`sidebar ${mobileMenu ? 'sidebar-open' : ''}`}>
        <a className="brand brand-sidebar" href="/" aria-label="Waybill">
          <span className="brand-mark"><Icon name="box" size={22} /></span>
          <span>waybill<span className="brand-dot">.</span></span>
        </a>
        <div className="sidebar-label">KHÔNG GIAN LÀM VIỆC</div>
        <nav className="side-nav" aria-label="Điều hướng chính">
          <button className="side-nav-item" type="button" onClick={() => setMobileMenu(false)}><Icon name="grid" /><span>Tổng quan</span></button>
          <button className="side-nav-item active" type="button" onClick={() => setMobileMenu(false)}><Icon name="box" /><span>Vận đơn</span><span className="nav-count">{totalItems}</span></button>
        </nav>
        <div className="sidebar-bottom">
          <div className="help-card"><span className="help-symbol">?</span><b>Cần hỗ trợ?</b><p>Đội ngũ Waybill luôn sẵn sàng giúp bạn.</p></div>
          <div className="sidebar-user">
            <span className="avatar">{(user.email || 'W').slice(0, 1).toUpperCase()}</span>
            <span className="user-text"><b>{user.email}</b><small>{user.role === 'ADMIN' ? 'Quản trị viên' : 'Tài xế'}</small></span>
            <button className="icon-button signout-button" type="button" aria-label="Đăng xuất" onClick={handleSignOut} disabled={signingOut}><Icon name="logout" size={18} /></button>
          </div>
        </div>
      </aside>
      {mobileMenu && <button className="mobile-scrim" type="button" aria-label="Đóng menu" onClick={() => setMobileMenu(false)} />}
      <main className="main-content">
        <header className="topbar">
          <button className="mobile-menu-button icon-button" type="button" aria-label="Mở menu" onClick={() => setMobileMenu(!mobileMenu)}><Icon name="menu" /></button>
          <div className="breadcrumb"><span>Không gian làm việc</span><Icon name="chevron" size={14} /><b>Vận đơn</b></div>
          <div className="topbar-right"><span className="connection-dot" /> Hệ thống đang hoạt động</div>
        </header>

        <div className="page-content">
          <section className="page-heading">
            <div><span className="eyebrow eyebrow-green">QUẢN LÝ GIAO NHẬN</span><h1>Vận đơn</h1><p>Theo dõi và quản lý toàn bộ đơn hàng của bạn.</p></div>
            {user.role === 'ADMIN' && <button className="button button-primary" type="button" onClick={() => setShowCreate(true)}><Icon name="plus" size={18} /> Tạo vận đơn</button>}
          </section>

          <section className="metrics" aria-label="Tổng quan vận đơn">
            <MetricCard label="Tổng vận đơn" value={totalItems} note="Trong hệ thống" icon="box" />
            <MetricCard label="Đang xử lý" value={activeItems} note="Trên trang hiện tại" icon="clock" accent="orange" />
            <MetricCard label="Đã giao thành công" value={deliveredItems} note="Trên trang hiện tại" icon="check" accent="green" />
          </section>

          <section className="shipment-panel">
            <div className="panel-heading">
              <div><h2>Danh sách vận đơn</h2><p>{totalItems ? `Đang hiển thị ${shipments.length} trên ${totalItems} vận đơn` : 'Các vận đơn được cập nhật mới nhất'}</p></div>
              <div className="panel-tools">
                <label className="search-box"><Icon name="search" size={18} /><input type="search" placeholder="Tìm trong trang này…" value={search} onChange={(event) => setSearch(event.target.value)} /></label>
                <select className="filter-select" aria-label="Lọc theo trạng thái" value={status} onChange={(event) => { setLoading(true); setStatus(event.target.value); setPage(0) }}>
                  {STATUS_OPTIONS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                </select>
              </div>
            </div>
            {error && <div className="error-banner" role="alert"><span>{error}</span><button type="button" onClick={() => { setLoading(true); setReloadKey((key) => key + 1) }}>Thử lại</button></div>}
            <div className="table-scroll">
              <table className="shipment-table">
                <thead><tr><th>MÃ VẬN ĐƠN</th><th>NGƯỜI NHẬN</th><th>ĐỊA CHỈ GIAO</th><th>TIỀN THU HỘ</th><th>TRẠNG THÁI</th><th>NGÀY TẠO</th><th /></tr></thead>
                <tbody>
                  {loading && <tr><td colSpan="7"><div className="table-message"><span className="spinner" />Đang tải vận đơn…</div></td></tr>}
                  {!loading && !error && visibleShipments.map((shipment) => (
                    <tr key={shipment.id} className="shipment-row" onClick={() => setSelected(shipment)}>
                      <td><span className="tracking-code">{shipment.trackingCode}</span><small className="shipment-number">#{shipment.id}</small></td>
                      <td><span className="recipient-name">{shipment.receiverName}</span><small className="recipient-phone">{shipment.receiverPhone}</small></td>
                      <td><span className="delivery-address">{shipment.deliveryAddress}</span></td>
                      <td><span className="money-value">{formatMoney(shipment.codAmount)}</span></td>
                      <td><span className={`status-badge status-${shipment.status?.toLowerCase()}`}><i />{STATUS_LABELS[shipment.status] || shipment.status}</span></td>
                      <td><span className="date-value">{formatDate(shipment.createdAt)}</span></td>
                      <td><button className="row-action icon-button" type="button" aria-label={`Xem vận đơn ${shipment.trackingCode}`} onClick={(event) => { event.stopPropagation(); setSelected(shipment) }}><Icon name="chevron" size={18} /></button></td>
                    </tr>
                  ))}
                  {!loading && !error && !visibleShipments.length && (
                    <tr><td colSpan="7"><div className="empty-state"><span className="empty-icon"><Icon name="box" size={24} /></span><b>{search ? 'Không tìm thấy vận đơn phù hợp' : 'Chưa có vận đơn nào'}</b><p>{search ? 'Thử từ khóa khác hoặc xóa bộ lọc tìm kiếm.' : 'Vận đơn mới sẽ xuất hiện tại đây.'}</p></div></td></tr>
                  )}
                  {!loading && error && <tr><td colSpan="7"><div className="table-message">Không thể tải dữ liệu vận đơn.</div></td></tr>}
                </tbody>
              </table>
            </div>
            <div className="table-footer">
              <span>{totalItems ? <>Trang <b>{page + 1}</b> / <b>{Math.max(totalPages, 1)}</b></> : 'Không có kết quả'}</span>
              <div className="pagination">
                <button className="button button-quiet" type="button" disabled={page === 0 || loading} onClick={() => { setLoading(true); setPage(page - 1) }}>Trước</button>
                <button className="button button-quiet" type="button" disabled={page + 1 >= totalPages || loading} onClick={() => { setLoading(true); setPage(page + 1) }}>Tiếp theo <Icon name="arrow" size={15} /></button>
              </div>
            </div>
          </section>
          <footer className="page-footer"><span>© {CURRENT_YEAR} Waybill Delivery Management</span><span>Quản lý giao nhận dễ dàng hơn.</span></footer>
        </div>
      </main>
      {showCreate && <ShipmentForm onClose={() => setShowCreate(false)} onCreated={handleCreated} />}
      {selected && <ShipmentDetails shipment={selected} role={user.role} onClose={() => setSelected(null)} onUpdated={handleUpdated} />}
    </div>
  )
}
