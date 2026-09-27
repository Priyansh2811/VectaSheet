import { useEffect } from 'react';
import { Routes, Route, useLocation } from 'react-router-dom';

import Landing from './pages/Landing';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import Dashboard from './pages/Dashboard';
import WorkspaceShell from './pages/WorkspaceShell';
import WorkspaceOverview from './pages/WorkspaceOverview';
import SpreadsheetsList from './pages/SpreadsheetsList';
import SpreadsheetEditor from './pages/SpreadsheetEditor';
import DocumentsList from './pages/DocumentsList';
import DocumentEditor from './pages/DocumentEditor';
import TasksList from './pages/TasksList';
import KanbanBoard from './pages/KanbanBoard';
import Calendar from './pages/Calendar';
import Files from './pages/Files';
import Activity from './pages/Activity';
import Settings from './pages/Settings';
import ModulePlaceholder from './pages/ModulePlaceholder';
import Contact from './pages/Contact';
import Waitlist from './pages/Waitlist';
import ThankYou from './pages/ThankYou';
import NotFound from './pages/NotFound';
import ProtectedRoute from './components/ProtectedRoute';
import CookieConsent from './components/CookieConsent';
import { trackPageview } from './services/analytics';

const MODULES = ['canvas', 'dashboard', 'automate'];

function AnalyticsListener() {
  const location = useLocation();
  useEffect(() => {
    trackPageview(location.pathname);
  }, [location.pathname]);
  return null;
}

export default function App() {
  return (
    <>
      <AnalyticsListener />
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/contact" element={<Contact />} />
        <Route path="/waitlist" element={<Waitlist />} />
        <Route path="/thank-you" element={<ThankYou />} />

        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/w/:workspaceId"
          element={
            <ProtectedRoute>
              <WorkspaceShell />
            </ProtectedRoute>
          }
        >
          <Route path="overview" element={<WorkspaceOverview />} />
          <Route path="sheets" element={<SpreadsheetsList />} />
          <Route path="sheets/:spreadsheetId" element={<SpreadsheetEditor />} />
          <Route path="docs" element={<DocumentsList />} />
          <Route path="docs/:documentId" element={<DocumentEditor />} />
          <Route path="tasks" element={<TasksList />} />
          <Route path="board" element={<KanbanBoard />} />
          <Route path="calendar" element={<Calendar />} />
          <Route path="files" element={<Files />} />
          <Route path="activity" element={<Activity />} />
          <Route path="settings" element={<Settings />} />
          {MODULES.map((m) => (
            <Route
              key={m}
              path={m}
              element={<ModulePlaceholder title={`${m[0].toUpperCase()}${m.slice(1)} is coming soon`} />}
            />
          ))}
        </Route>

        <Route path="*" element={<NotFound />} />
      </Routes>
      <CookieConsent />
    </>
  );
}
