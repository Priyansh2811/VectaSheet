import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [remember, setRemember] = useState(true);
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  const { login } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  function validate() {
    const errs = {};
    if (!email.trim()) errs.email = 'Email is required';
    if (!password) errs.password = 'Password is required';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!validate()) return;

    setSubmitting(true);
    try {
      await login({ email, password });
      showToast('Welcome back', 'success');
      const dest = location.state?.from || '/dashboard';
      navigate(dest, { replace: true });
    } catch (err) {
      showToast(err.message || 'Unable to sign in', 'error');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthLayout title="Sign in to VectaSheet" subtitle="Continue where you left off.">
      <form onSubmit={handleSubmit} noValidate>
        <div className="field">
          <label>Email</label>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoFocus />
          {errors.email && <span className="field-error">{errors.email}</span>}
        </div>
        <div className="field">
          <label>Password</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
          {errors.password && <span className="field-error">{errors.password}</span>}
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20, fontSize: 13 }}>
          <label style={{ display: 'flex', alignItems: 'center', gap: 6, color: 'var(--text-secondary)' }}>
            <input type="checkbox" checked={remember} onChange={(e) => setRemember(e.target.checked)} />
            Remember me
          </label>
          <Link to="/forgot-password" style={{ color: 'var(--accent)' }}>Forgot password?</Link>
        </div>
        <button className="btn btn-primary" style={{ width: '100%' }} disabled={submitting}>
          {submitting ? 'Signing in…' : 'Sign In'}
        </button>
      </form>
      <p style={{ marginTop: 18, fontSize: 13, color: 'var(--text-secondary)', textAlign: 'center' }}>
        Don't have an account? <Link to="/register" style={{ color: 'var(--accent)' }}>Create one</Link>
      </p>
      <p style={{ marginTop: 10, fontSize: 12, color: 'var(--text-tertiary)', textAlign: 'center' }}>
      </p>
    </AuthLayout>
  );
}

export function AuthLayout({ title, subtitle, children }) {
  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--bg)', padding: 20 }}>
      <div style={{ width: '100%', maxWidth: 380 }}>
        <div style={{ textAlign: 'center', marginBottom: 28 }}>
          <Link to="/" style={{ fontWeight: 700, fontSize: 17 }}>VectaSheet</Link>
        </div>
        <div className="card" style={{ padding: 28 }}>
          <h1 style={{ fontSize: 18, fontWeight: 700, marginBottom: 4 }}>{title}</h1>
          <p style={{ fontSize: 13, color: 'var(--text-secondary)', marginBottom: 22 }}>{subtitle}</p>
          {children}
        </div>
      </div>
    </div>
  );
}
