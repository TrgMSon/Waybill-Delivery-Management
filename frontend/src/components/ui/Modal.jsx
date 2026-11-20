import { useEffect } from 'react';
import Icon from './Icon';

export default function Modal({ title, subtitle, onClose, children, wide = false }) {
  useEffect(() => {
    function handleKeyDown(event) {
      if (event.key === 'Escape') onClose();
    }
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  return (
    <div className="modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}>
      <section className={`modal ${wide ? 'modal-wide' : ''}`} role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <div className="modal-heading">
          <div><h2 id="modal-title">{title}</h2><p>{subtitle}</p></div>
          <button className="icon-button" type="button" aria-label="Đóng" onClick={onClose}><Icon name="close" /></button>
        </div>
        {children}
      </section>
    </div>
  )
}
