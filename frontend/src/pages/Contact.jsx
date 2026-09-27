import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useToast } from '../context/ToastContext';

export default function Contact() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const { showToast } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    document.title = 'Contact — VectaSheet';
  }, []);

  function validate() {
    const errs = {};
    if (!name.trim()) errs.name = 'Name is required';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errs.email = 'Enter a valid email address';
    if (!message.trim()) errs.message = 'Message is required';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  }

  function handleSubmit(e) {
    e.preventDefault();
    if (!validate()) return;
    // No backend contact-form endpoint exists yet in this build phase, so we
    // don't pretend to send anything — we're upfront that it's a mailto handoff.
    setSubmitting(true);
    window.location.href = `mailto:hello@vectasheet.example?subject=${encodeURIComponent('Message from ' + name)}&body=${encodeURIComponent(message + '\n\n' + email)}`;
    setSubmitting(false);
    showToast('Opening your email client…', 'success');
    navigate('/thank-you');
  }

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg)' }}>
      <nav style={{ padding: '16px 32px', borderBottom: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between' }}>
        <Link to="/" style={{ fontWeight: 700 }}>VectaSheet</Link>
        <div style={{ fontSize: 12.5, color: 'var(--text-tertiary)' }}>
          <Link to="/" style={{ color: 'var(--text-tertiary)' }}>Home</Link> / Contact
        </div>
      </nav>
      <div style={{ maxWidth: 460, margin: '0 auto', padding: '60px 24px' }}>
        <h1 style={{ fontSize: 22, fontWeight: 700, marginBottom: 6 }}>Get in touch</h1>
        <p style={{ fontSize: 13.5, color: 'var(--text-secondary)', marginBottom: 26 }}>
          Questions, feedback, or partnership ideas — we'd like to hear from you.
        </p>
        <form onSubmit={handleSubmit} noValidate>
          <div className="field">
            <label>Name</label>
            <input value={name} onChange={(e) => setName(e.target.value)} />
            {errors.name && <span className="field-error">{errors.name}</span>}
          </div>
          <div className="field">
            <label>Email</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
            {errors.email && <span className="field-error">{errors.email}</span>}
          </div>
          <div className="field">
            <label>Message</label>
            <textarea rows={5} value={message} onChange={(e) => setMessage(e.target.value)} />
            {errors.message && <span className="field-error">{errors.message}</span>}
          </div>
          <button className="btn btn-primary" style={{ width: '100%' }} disabled={submitting}>
            {submitting ? 'Sending…' : 'Send message'}
          </button>
        </form>
      </div>
    </div>
  );
}
