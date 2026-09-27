export default function Loading({ full = false }) {
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        height: full ? '100vh' : '160px',
        color: 'var(--text-tertiary)',
        fontSize: 13.5,
      }}
    >
      Loading…
    </div>
  );
}
