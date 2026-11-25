import { useState } from 'react';
import { register } from '../../api'
import Icon from '../ui/Icon';

const CURRENT_YEAR = new Date().getFullYear();

export default function LoginScreen({ onLogin }) {
  const [mode, setMode] = useState('login')
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('')
  const [phone, setPhone] = useState('')
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('')
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setNotice('')
    setLoading(true);
    try {
      if (mode === 'register') {
        const result = await register({ email, password, fullName, phone: phone || null })
        setMode('login')
        setPassword('')
        setNotice(result.message || 'Tạo tài khoản thành công. Hãy đăng nhập.')
      } else {
        await onLogin({ email, password })
      }
    } catch (formError) {
      setError(formError.message)
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-page">
      <section className="login-story">
        <a className="brand brand-light" href="/" aria-label="Waybill home">
          <span className="brand-mark"><Icon name="box" size={23} /></span>
          <span>Waybill Delivery Management<span className="brand-dot">.</span></span>
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
          <span className="eyebrow eyebrow-green">{mode === 'login' ? 'CHÀO MỪNG TRỞ LẠI' : 'THAM GIA WAYBILL'}</span>
          <h2>{mode === 'login' ? 'Đăng nhập' : 'Tạo tài khoản'}</h2>
          <p className="login-intro">{mode === 'login' ? 'Đăng nhập để quản lý vận đơn của bạn.' : 'Đăng ký tài khoản tài xế để bắt đầu.'}</p>
          <form className="login-form" onSubmit={handleSubmit}>
            {mode === 'register' && (
              <>
                <label htmlFor="fullName">Họ và tên</label>
                <input id="fullName" type="text" autoComplete="name" placeholder="Nguyễn Văn A" value={fullName} onChange={(event) => setFullName(event.target.value)} maxLength={150} required />
                <label className="register-phone-label" htmlFor="phone">Số điện thoại <span>(không bắt buộc)</span></label>
                <input id="phone" type="tel" autoComplete="tel" placeholder="0901234567" value={phone} onChange={(event) => setPhone(event.target.value)} maxLength={20} />
              </>
            )}
            <label htmlFor="email">Email</label>
            <input id="email" type="email" autoComplete="username" placeholder="ten@congty.vn" value={email} onChange={(event) => setEmail(event.target.value)} required />
            <div className="password-label"><label htmlFor="password">Mật khẩu</label></div>
            <input id="password" type="password" autoComplete={mode === 'login' ? 'current-password' : 'new-password'} placeholder={mode === 'register' ? 'Ít nhất 8 ký tự' : 'Nhập mật khẩu'} value={password} onChange={(event) => setPassword(event.target.value)} minLength={mode === 'register' ? 8 : undefined} required />
            {notice && <p className="form-success" role="status">{notice}</p>}
            {error && <p className="form-error" role="alert">{error}</p>}
            <button className="button button-primary login-submit" type="submit" disabled={loading}>
              {loading ? 'Đang xử lý…' : mode === 'login' ? 'Đăng nhập' : 'Tạo tài khoản'} <Icon name="arrow" size={18} />
            </button>
          </form>
          <button
            className="auth-mode-toggle"
            type="button"
            onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); setNotice('') }}
          >
            {mode === 'login' ? 'Chưa có tài khoản? Đăng ký tài xế' : 'Đã có tài khoản? Đăng nhập'}
          </button>
          {mode === 'login' && import.meta.env.DEV && (
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
