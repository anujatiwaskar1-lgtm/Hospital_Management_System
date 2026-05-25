import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Login({ onLogin }) {
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const nav = useNavigate();

  const handle = (e) => {
    e.preventDefault();
    if (form.username === 'admin' && form.password === 'admin123') {
      onLogin({ username: 'admin', role: 'Admin' });
      nav('/dashboard');
    } else {
      setError('Invalid credentials. Try admin / admin123');
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', background: 'linear-gradient(135deg, #0f172a 0%, #1e3a5f 100%)' }}>
      {/* Left panel */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', padding: 60 }}>
        <div style={{ fontSize: 42, fontWeight: 800, color: '#fff', marginBottom: 8 }}>
          Medi<span style={{ color: '#0ea5e9' }}>Core</span>
        </div>
        <div style={{ color: '#94a3b8', marginBottom: 48 }}>Hospital Management System</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, width: '100%', maxWidth: 340 }}>
          {[['500+','Beds Managed'],['98%','Uptime'],['24/7','Support'],['50+','Departments']].map(([n,l]) => (
            <div key={l} style={{ background: 'rgba(255,255,255,0.07)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 12, padding: '20px 24px' }}>
              <div style={{ fontSize: 28, fontWeight: 700, color: '#0ea5e9' }}>{n}</div>
              <div style={{ fontSize: 12, color: '#94a3b8', marginTop: 4 }}>{l}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Right panel */}
      <div style={{ width: 460, background: '#fff', display: 'flex', flexDirection: 'column', justifyContent: 'center', padding: '60px 50px' }}>
        <div style={{ fontSize: 28, fontWeight: 700, marginBottom: 6 }}>Welcome back</div>
        <div style={{ color: '#64748b', fontSize: 14, marginBottom: 36 }}>Sign in to your account</div>

        {error && <div style={{ background: '#fef2f2', color: '#dc2626', padding: '10px 14px', borderRadius: 8, fontSize: 13, marginBottom: 16 }}>{error}</div>}

        <form onSubmit={handle}>
          {[['username','Username','text'],['password','Password','password']].map(([k,label,type]) => (
            <div key={k}>
              <div style={{ fontSize: 13, fontWeight: 600, color: '#374151', marginBottom: 6 }}>{label}</div>
              <input type={type} value={form[k]} onChange={e => setForm({...form,[k]:e.target.value})}
                placeholder={`Enter ${label.toLowerCase()}`}
                style={{ width: '100%', padding: '12px 16px', border: '1.5px solid #e2e8f0', borderRadius: 8, fontSize: 14, outline: 'none', marginBottom: 20 }} />
            </div>
          ))}
          <button type="submit" style={{ width: '100%', padding: 13, background: '#0ea5e9', color: '#fff', border: 'none', borderRadius: 8, fontSize: 15, fontWeight: 600, cursor: 'pointer' }}>
            Sign In →
          </button>
        </form>

        <div style={{ marginTop: 24, textAlign: 'center', fontSize: 13, color: '#64748b' }}>
          Don't have an account?{' '}
          <span onClick={() => nav('/register')} style={{ color: '#0ea5e9', cursor: 'pointer', fontWeight: 600 }}>Register</span>
        </div>
      </div>
    </div>
  );
}