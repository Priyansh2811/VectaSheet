import { useState } from 'react';
import { Link } from 'react-router-dom';
import { apiRequest } from '../services/api';
import { AuthLayout } from './Login';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [sent, setSent] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    if (!email.trim()) {
      setError('Email is required');
      return;
    }
    setError('');
    setSubmitting(true);
    try {
      await apiRequest('/auth/forgot-password', { method: 'POST', body: { email } });
      setSent(true);
    } catch (err) {
      // We deliberately show the same success state even on failure, so we never
      // reveal whether an email address has an account.
      setSent(true);
    } finally {
      setSubmitting(false);
    }
  }

  if (sent) {
    return (
      <AuthLayout title="Check your email" subtitle="">
        <p style={{ fontSize: 13.5, color: 'var(--text-secondary)', lineHeight: 1.6 }}>
          If an account exists for <strong>{email}</strong>, we've sent a link to reset your password.
          It expires in 30 minutes.
        </p>
        <Link to="/login" className="btn btn-secondary" style={{ width: '100%', marginTop: 18, justifyContent: 'center' }}>
          Back to sign in
        </Link>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout title="Reset your password" subtitle="Enter your email and we'll send you a reset link.">
      <form onSubmit={handleSubmit} noValidate>
        <div className="field">
          <label>Email</label>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoFocus />
          {error && <span className="field-error">{error}</span>}
        </div>
        <button className="btn btn-primary" style={{ width: '100%' }} disabled={submitting}>
          {submitting ? 'Sending…' : 'Send reset link'}
        </button>
      </form>
      <p style={{ marginTop: 18, fontSize: 13, color: 'var(--text-secondary)', textAlign: 'center' }}>
        <Link to="/login" style={{ color: 'var(--accent)' }}>Back to sign in</Link>
      </p>
    </AuthLayout>
  );
}
