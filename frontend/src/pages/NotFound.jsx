import { useEffect } from 'react';
import { Link } from 'react-router-dom';

export default function NotFound() {
  useEffect(() => {
    document.title = 'Page not found — VectaSheet';
  }, []);

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 12, padding: 20, textAlign: 'center' }}>
      <div style={{ fontSize: 13, color: 'var(--text-tertiary)' }}>404</div>
      <h1 style={{ fontSize: 22, fontWeight: 700 }}>This page doesn't exist</h1>
      <p style={{ fontSize: 13.5, color: 'var(--text-secondary)', maxWidth: 360 }}>
        The link may be broken, or the page may have moved. Let's get you back on track.
      </p>
      <div style={{ display: 'flex', gap: 10, marginTop: 8 }}>
        <Link to="/" className="btn btn-secondary">Go to homepage</Link>
        <Link to="/dashboard" className="btn btn-primary">Go to dashboard</Link>
      </div>
    </div>
  );
}
