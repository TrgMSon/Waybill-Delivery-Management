import { useState } from 'react'
import Icon from '../ui/Icon'

const CURRENT_YEAR = new Date().getFullYear()

export default function LoginScreen({ onLogin }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      await onLogin({ email, password })
    } catch (loginError) {
      setError(loginError.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="login-page">
      <section className="login-story">
        <a className="brand brand-light" href="/" aria-label="Waybill home">
          <span className="brand-mark"><Icon name="box" size={23} /></span>
          <span>waybill<span className="brand-dot">.</span></span>
        </a>
        <div className="story-copy">
          <span className="eyebrow">NỀN TẢNG ĐIỀU PHỐI GIAO HÀNG</span>
          <h1>Mỗi đơn hàng,<br />một hành trình<br /><em>đúng hẹn.</em></h1>
          <p>Theo dõi vận đơn, tối ưu quy trình và mang đến trải nghiệm giao hàng đáng tin cậy.</p>
        </div>
        <div className="route-art" aria-hidden="true">
          <div className="route-line" />
          <span className="route-point point-a"><Icon name="box" size={18} /></span>
          <span className="route-point point-b"><Icon name="pin" size={18} /></span>
          <div className="route-card"><span className="route-card-icon"><Icon name="check" size={16} /></span><span><b>Đơn hàng đang đến</b><small>Hành trình được cập nhật liên tục</small></span></div>
        </div>
        <div className="story-footer"><span>ĐIỀU PHỐI THÔNG MINH</span><span>© WAYBILL {CURRENT_YEAR}</span></div>
      </section>

      <section className="login-panel">
        <div className="mobile-brand"><span className="brand-mark"><Icon name="box" size={21} /></span> waybill<span className="brand-dot">.</span></div>
        <div className="login-form-wrap">
          <span className="eyebrow eyebrow-green">CHÀO MỪNG TRỞ LẠI</span>
          <h2>Đăng nhập</h2>
          <p className="login-intro">Đăng nhập để quản lý vận đơn của bạn.</p>
          <form className="login-form" onSubmit={handleSubmit}>
            <label htmlFor="email">Email</label>
            <input id="email" type="email" autoComplete="username" placeholder="ten@congty.vn" value={email} onChange={(event) => setEmail(event.target.value)} required />
            <div className="password-label"><label htmlFor="password">Mật khẩu</label></div>
            <input id="password" type="password" autoComplete="current-password" placeholder="Nhập mật khẩu" value={password} onChange={(event) => setPassword(event.target.value)} required />
            {error && <p className="form-error" role="alert">{error}</p>}
            <button className="button button-primary login-submit" type="submit" disabled={loading}>
              {loading ? 'Đang đăng nhập…' : 'Đăng nhập'} <Icon name="arrow" size={18} />
            </button>
          </form>
          {import.meta.env.DEV && (
            <div className="demo-hint">
              <span className="demo-icon"><Icon name="check" size={15} /></span>
              <p><b>Tài khoản demo</b><br />admin@demo.tms <span>·</span> Demo@123</p>
            </div>
          )}
        </div>
        <p className="login-legal">Bằng việc tiếp tục, bạn đồng ý với điều khoản sử dụng và chính sách bảo mật.</p>
      </section>
    </main>
  )
}
