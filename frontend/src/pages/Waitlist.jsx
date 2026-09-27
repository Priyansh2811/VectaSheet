import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiRequest } from '../services/api';
import { useToast } from '../context/ToastContext';

export default function Waitlist() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { showToast } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    document.title = 'Join the waitlist — VectaSheet';
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError('Enter a valid email address');
      return;
    }
    setError('');
    setSubmitting(true);
    try {
      // No dedicated waitlist table exists yet in this build phase — registering
      // a real account is the honest equivalent of "join the waitlist" for now.
      showToast('Thanks — head to Sign up to reserve your workspace now.', 'success');
      navigate('/register');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--bg)', padding: 20 }}>
      <div style={{ maxWidth: 380, width: '100%', textAlign: 'center' }}>
        <Link to="/" style={{ fontWeight: 700, fontSize: 17 }}>VectaSheet</Link>
        <h1 style={{ fontSize: 20, fontWeight: 700, marginTop: 18 }}>Join the waitlist</h1>
        <p style={{ fontSize: 13.5, color: 'var(--text-secondary)', marginTop: 8, marginBottom: 22 }}>
          VectaSheet is free to start today — no waitlist needed. Pop in your email and we'll take you straight to sign-up.
        </p>
        <form onSubmit={handleSubmit} noValidate>
          <div className="field">
            <input placeholder="you@company.com" value={email} onChange={(e) => setEmail(e.target.value)} />
            {error && <span className="field-error">{error}</span>}
          </div>
          <button className="btn btn-primary" style={{ width: '100%' }} disabled={submitting}>
            {submitting ? 'One sec…' : 'Get started'}
          </button>
        </form>
      </div>
    </div>
  );
}
