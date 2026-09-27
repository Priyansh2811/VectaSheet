import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { apiRequest } from '../services/api';
import { useToast } from '../context/ToastContext';
import { AuthLayout } from './Login';

export default function ResetPassword() {
  const [params] = useSearchParams();
  const token = params.get('token') || '';
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  const { showToast } = useToast();
  const navigate = useNavigate();

  function validate() {
    const errs = {};
    if (!token) errs.token = 'This reset link is missing its token';
    if (!password || password.length < 8) errs.password = 'Password must be at least 8 characters';
    if (confirmPassword !== password) errs.confirmPassword = 'Passwords do not match';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!validate()) return;

    setSubmitting(true);
    try {
      await apiRequest('/auth/reset-password', { method: 'POST', body: { token, newPassword: password } });
      showToast('Password updated — please sign in', 'success');
      navigate('/login', { replace: true });
    } catch (err) {
      showToast(err.message || 'This reset link is invalid or has expired', 'error');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthLayout title="Set a new password" subtitle="Choose a strong password for your account.">
      {!token && (
        <p className="field-error" style={{ marginBottom: 16 }}>
          This link is missing a reset token. Please request a new one.
        </p>
      )}
      <form onSubmit={handleSubmit} noValidate>
        <div className="field">
          <label>New password</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoFocus />
          {errors.password && <span className="field-error">{errors.password}</span>}
        </div>
        <div className="field">
          <label>Confirm new password</label>
          <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} />
          {errors.confirmPassword && <span className="field-error">{errors.confirmPassword}</span>}
        </div>
        <button className="btn btn-primary" style={{ width: '100%' }} disabled={submitting || !token}>
          {submitting ? 'Updating…' : 'Update password'}
        </button>
      </form>
      <p style={{ marginTop: 18, fontSize: 13, color: 'var(--text-secondary)', textAlign: 'center' }}>
        <Link to="/login" style={{ color: 'var(--accent)' }}>Back to sign in</Link>
      </p>
    </AuthLayout>
  );
}
