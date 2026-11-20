import { useEffect, useState } from 'react';
import { login, logout, refreshSession } from './api';
import LoginScreen from './components/auth/LoginScreen';
import Dashboard from './components/dashboard/Dashboard';
import './App.css';

export default function App() {
  const [user, setUser] = useState(null);
  const [checkingSession, setCheckingSession] = useState(true);
  const [sessionError, setSessionError] = useState('');

  useEffect(() => {
    let active = true;
    refreshSession()
      .then((session) => {
        if (active && session) setUser(session.userResponse);
      })
      .catch((error) => {
        if (active) setSessionError(error.message);
      })
      .finally(() => {
        if (active) setCheckingSession(false);
      })
    return () => { active = false; }
  }, []);

  async function handleLogin(credentials) {
    const session = await login(credentials);
    setSessionError('');
    setUser(session.userResponse);
  }

  async function handleLogout() {
    try {
      await logout();
      setSessionError('');
    } catch (error) {
      setSessionError(error.message);
      throw error;
    } finally {
      setUser(null);
    }
  }

  if (checkingSession) {
    return <main className="session-loading"><span className="spinner" /><span>Đang kiểm tra phiên đăng nhập…</span></main>
  }
  if (user) return <Dashboard user={user} onSignOut={handleLogout} />
  return (
    <>
      {sessionError && <div className="session-warning" role="alert">{sessionError}</div>}
      <LoginScreen onLogin={handleLogin} />
    </>
  )
}
