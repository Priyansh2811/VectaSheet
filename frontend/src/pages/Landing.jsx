import { Link } from 'react-router-dom';
import ThemeToggle from '../components/ThemeToggle';

const FEATURES = [
  { title: 'Collaborative Spreadsheet', desc: 'A real formula engine with dependency-aware recalculation, multiple sheets and rich formatting.' },
  { title: 'Infinite Canvas', desc: 'Shapes, freehand drawing, sticky notes and frames that stay connected to your data.' },
  { title: 'Smart Tasks', desc: 'Tasks, subtasks, priorities and dependencies that sync straight to your board and calendar.' },
  { title: 'Live Dashboards', desc: 'Charts and widgets built from real workspace data, not static screenshots.' },
  { title: 'Automation', desc: 'Trigger → condition → action workflows that run in the background.' },
  { title: 'Real-Time Collaboration', desc: 'See presence, edits and cursors as your team works alongside you.' },
  { title: 'Version History', desc: 'Every change recorded, every version restorable without losing history.' },
  { title: 'Templates', desc: 'Start from a working project tracker, roadmap, CRM or sprint board.' },
];

const USE_CASES = ['Product teams', 'Developers', 'Designers', 'Marketing', 'Students', 'Startups', 'Operations'];

const FAQS = [
  { q: 'Is VectaSheet free to start?', a: 'Yes — you can create a workspace and start working immediately with the free tier. No credit card required.' },
  { q: 'Can multiple people edit the same sheet or canvas at once?', a: 'Yes. VectaSheet is built for real-time collaboration with presence indicators and conflict-safe updates.' },
  { q: 'Does VectaSheet support formulas like a normal spreadsheet?', a: 'Yes. Core functions like SUM, IF, VLOOKUP and XLOOKUP are supported with dependency-aware recalculation.' },
  { q: 'Can I import my existing spreadsheets?', a: 'Yes. You can import CSV, XLSX and JSON files directly into a workspace.' },
  { q: 'What happens if two people edit the same cell at once?', a: 'VectaSheet detects the conflict and lets you choose which version to keep instead of silently overwriting either change.' },
];

export default function Landing() {
  return (
    <div style={{ background: 'var(--bg)', color: 'var(--text)', minHeight: '100vh' }}>
      <nav
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '16px 32px',
          borderBottom: '1px solid var(--border)',
          position: 'sticky',
          top: 0,
          background: 'var(--bg)',
          zIndex: 10,
        }}
      >
        <div style={{ fontWeight: 700, fontSize: 16, letterSpacing: '-0.01em' }}>VectaSheet</div>
        <div style={{ display: 'flex', gap: 28, fontSize: 13.5, color: 'var(--text-secondary)' }}>
          <a href="#features">Product</a>
          <a href="#use-cases">Solutions</a>
          <Link to="/templates">Templates</Link>
          <a href="#faq">Pricing</a>
        </div>
        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          <ThemeToggle />
          <Link to="/login" className="btn btn-ghost">Sign In</Link>
          <Link to="/register" className="btn btn-primary">Get Started</Link>
        </div>
      </nav>

      <section style={{ maxWidth: 780, margin: '0 auto', textAlign: 'center', padding: '100px 24px 60px' }}>
        <h1 style={{ fontSize: 44, fontWeight: 700, letterSpacing: '-0.02em', lineHeight: 1.15 }}>
          Think visually. Work with data. Execute together.
        </h1>
        <p style={{ marginTop: 18, fontSize: 16.5, color: 'var(--text-secondary)', lineHeight: 1.6 }}>
          VectaSheet brings collaborative spreadsheets, infinite canvas, tasks, dashboards and workflows
          into one connected workspace.
        </p>
        <div style={{ marginTop: 28, display: 'flex', gap: 12, justifyContent: 'center' }}>
          <Link to="/register" className="btn btn-primary" style={{ padding: '11px 22px' }}>Start Free</Link>
          <Link to="/login" className="btn btn-secondary" style={{ padding: '11px 22px' }}>Explore Demo</Link>
        </div>
      </section>

      <section style={{ maxWidth: 1040, margin: '0 auto', padding: '0 24px 80px' }}>
        <div className="card" style={{ padding: 8, boxShadow: 'var(--shadow)' }}>
          <div
            style={{
              display: 'flex',
              height: 380,
              border: '1px solid var(--border)',
              borderRadius: 8,
              overflow: 'hidden',
            }}
          >
            <div style={{ width: 180, background: 'var(--sidebar-bg)', borderRight: '1px solid var(--border)', padding: 14, fontSize: 12.5, color: 'var(--text-secondary)' }}>
              <div style={{ fontWeight: 600, marginBottom: 12, color: 'var(--text)' }}>Demo Workspace</div>
              {['Overview', 'Canvas', 'Sheets', 'Tasks', 'Board', 'Calendar', 'Dashboard'].map((s) => (
                <div key={s} style={{ padding: '6px 0' }}>{s}</div>
              ))}
            </div>
            <div style={{ flex: 1, padding: 24, display: 'flex', flexDirection: 'column', gap: 12 }}>
              <div style={{ height: 14, width: '40%' }} className="skeleton" />
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 10, marginTop: 8 }}>
                {[1, 2, 3, 4].map((i) => <div key={i} style={{ height: 70 }} className="skeleton" />)}
              </div>
              <div style={{ height: 160, marginTop: 8 }} className="skeleton" />
            </div>
          </div>
        </div>
      </section>

      <section id="features" style={{ maxWidth: 1040, margin: '0 auto', padding: '20px 24px 80px' }}>
        <h2 style={{ fontSize: 24, fontWeight: 700, marginBottom: 28, letterSpacing: '-0.01em' }}>One workspace, every view</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(230px, 1fr))', gap: 20 }}>
          {FEATURES.map((f) => (
            <div key={f.title} className="card" style={{ padding: 18 }}>
              <h3 style={{ fontSize: 14.5, fontWeight: 600, marginBottom: 8 }}>{f.title}</h3>
              <p style={{ fontSize: 13, color: 'var(--text-secondary)', lineHeight: 1.55 }}>{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      <section id="use-cases" style={{ maxWidth: 1040, margin: '0 auto', padding: '0 24px 80px' }}>
        <h2 style={{ fontSize: 24, fontWeight: 700, marginBottom: 20, letterSpacing: '-0.01em' }}>Built for how your team already works</h2>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
          {USE_CASES.map((u) => (
            <span key={u} style={{ padding: '8px 14px', borderRadius: 20, border: '1px solid var(--border-strong)', fontSize: 13 }}>{u}</span>
          ))}
        </div>
      </section>

      <section id="faq" style={{ maxWidth: 720, margin: '0 auto', padding: '0 24px 100px' }}>
        <h2 style={{ fontSize: 24, fontWeight: 700, marginBottom: 24, letterSpacing: '-0.01em' }}>Frequently asked questions</h2>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          {FAQS.map((f) => (
            <div key={f.q} className="card" style={{ padding: '16px 18px' }}>
              <h3 style={{ fontSize: 14, fontWeight: 600, marginBottom: 6 }}>{f.q}</h3>
              <p style={{ fontSize: 13, color: 'var(--text-secondary)', lineHeight: 1.55 }}>{f.a}</p>
            </div>
          ))}
        </div>
      </section>

      <footer style={{ borderTop: '1px solid var(--border)', padding: '40px 32px' }}>
        <div style={{ maxWidth: 1040, margin: '0 auto', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: 24, fontSize: 13, color: 'var(--text-secondary)' }}>
          <div>
            <div style={{ fontWeight: 600, color: 'var(--text)', marginBottom: 10 }}>Product</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              <a href="#features">Features</a>
              <Link to="/templates">Templates</Link>
              <a href="#faq">Pricing</a>
            </div>
          </div>
          <div>
            <div style={{ fontWeight: 600, color: 'var(--text)', marginBottom: 10 }}>Resources</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              <a href="#faq">FAQ</a>
              <Link to="/contact">Contact</Link>
            </div>
          </div>
          <div>
            <div style={{ fontWeight: 600, color: 'var(--text)', marginBottom: 10 }}>Company</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              <Link to="/contact">Contact</Link>
              <Link to="/waitlist">Join waitlist</Link>
            </div>
          </div>
          <div>
            <div style={{ fontWeight: 600, color: 'var(--text)', marginBottom: 10 }}>Legal</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              <span>Privacy</span>
              <span>Terms</span>
            </div>
          </div>
          <div>
            <div style={{ fontWeight: 600, color: 'var(--text)', marginBottom: 10 }}>GitHub</div>
            <a href="https://github.com" target="_blank" rel="noreferrer">github.com/vectasheet</a>
          </div>
        </div>
        <div style={{ maxWidth: 1040, margin: '32px auto 0', fontSize: 12, color: 'var(--text-tertiary)' }}>
          © {new Date().getFullYear()} VectaSheet. All rights reserved.
        </div>
      </footer>
    </div>
  );
}
