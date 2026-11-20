import Icon from '../ui/Icon';

export default function MetricCard({ label, value, note, icon, accent = '' }) {
  return (
    <article className={`metric-card ${accent ? `metric-${accent}` : ''}`}>
      <div className="metric-top"><span>{label}</span><span className="metric-icon"><Icon name={icon} size={19} /></span></div>
      <div className="metric-value">{value.toLocaleString('vi-VN')}</div>
      <div className="metric-note"><span className="metric-dot" />{note}</div>
    </article>
  )
}
