import { useTheme } from '../context/ThemeContext';

export default function ThemeToggle() {
  const { theme, toggleTheme } = useTheme();

  return (
    <button
      className="btn btn-ghost"
      onClick={toggleTheme}
      title={theme === 'light' ? 'Switch to dark mode' : 'Switch to light mode'}
      style={{ padding: '8px 10px' }}
    >
      {theme === 'light' ? '◐ Dark' : '◑ Light'}
    </button>
  );
}
