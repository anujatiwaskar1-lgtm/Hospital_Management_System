import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

export default function Layout({ children, onLogout }) {
  const nav = useNavigate();
  const { pathname } = useLocation();

  const links = [
    { icon: '🏠', label: 'Dashboard', path: '/dashboard' },
    { icon: '👥', label: 'Patients',  path: '/patients'  },
    { icon: '🛏️', label: 'Beds',      path: '/beds'      },
  ];

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#f0f4f8' }}>
      {/* Sidebar */}
      <div style={{ width: 230, background: '#0f172a', display: 'flex', flexDirection: 'column', padding: '24px 0', position: 'fixed', top: 0, left: 0, height: '100vh' }}>
        <div style={{ padding: '0 24px 32px', fontSize: 22, fontWeight: 800, color: '#fff' }}>
          Medi<span style={{ color: '#0ea5e9' }}>Core</span>
          <div style={{ fontSize: 11, color: '#475569', fontWeight: 400, marginTop: 2 }}>Hospital Management</div>
        </div>

        {links.map(l => (
          <div key={l.path} onClick={() => nav(l.path)} style={{
            padding: '12px 24px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 12,
            background: pathname === l.path ? '#1e293b' : 'transparent',
            borderLeft: pathname === l.path ? '3px solid #0ea5e9' : '3px solid transparent',
            color: pathname === l.path ? '#fff' : '#94a3b8',
            fontSize: 14, fontWeight: 500, transition: 'all 0.2s',
          }}>
            <span style={{ fontSize: 16 }}>{l.icon}</span> {l.label}
          </div>
        ))}

        <div style={{ marginTop: 'auto', padding: '16px 24px', borderTop: '1px solid #1e293b' }}>
          <div onClick={onLogout} style={{ color: '#ef4444', cursor: 'pointer', fontSize: 14, display: 'flex', alignItems: 'center', gap: 8 }}>
            ⬅ Logout
          </div>
        </div>
      </div>

      {/* Main content */}
      <div style={{ marginLeft: 230, flex: 1, padding: '36px 40px' }}>
        {children}
      </div>
    </div>
  );
}