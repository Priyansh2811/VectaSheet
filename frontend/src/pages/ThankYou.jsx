import { useEffect } from 'react';
import { Link } from 'react-router-dom';

export default function ThankYou() {
  useEffect(() => {
    document.title = 'Thank you — VectaSheet';
  }, []);

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 12, padding: 20, textAlign: 'center' }}>
      <h1 style={{ fontSize: 22, fontWeight: 700 }}>Thank you</h1>
      <p style={{ fontSize: 13.5, color: 'var(--text-secondary)', maxWidth: 360 }}>
        We've received your message and will get back to you soon.
      </p>
      <Link to="/" className="btn btn-primary" style={{ marginTop: 8 }}>Back to homepage</Link>
    </div>
  );
}
