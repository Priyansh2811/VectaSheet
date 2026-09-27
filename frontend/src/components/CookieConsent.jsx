import { useEffect, useState } from 'react';
import { getConsent, setConsent } from '../services/analytics';

export default function CookieConsent() {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    setVisible(getConsent() === null);
  }, []);

  if (!visible) return null;

  function respond(value) {
    setConsent(value);
    setVisible(false);
  }

  return (
    <div
      style={{
        position: 'fixed',
        bottom: 16,
        left: 16,
        right: 16,
        maxWidth: 460,
        margin: '0 auto',
        background: 'var(--surface)',
        border: '1px solid var(--border-strong)',
        borderRadius: 10,
        padding: 16,
        boxShadow: 'var(--shadow)',
        zIndex: 2000,
      }}
    >
      <p style={{ fontSize: 13, color: 'var(--text-secondary)', marginBottom: 12, lineHeight: 1.5 }}>
        We use essential cookies to keep you signed in, and optional analytics cookies to understand how VectaSheet is used.
      </p>
      <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
        <button className="btn btn-ghost" onClick={() => respond('rejected')}>Reject</button>
        <button className="btn btn-primary" onClick={() => respond('accepted')}>Accept</button>
      </div>
    </div>
  );
}
