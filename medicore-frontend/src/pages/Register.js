import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function Register() {
  const [form, setForm] = useState({ username: '', password: '', email: '', role: 'DOCTOR' });
  const [msg, setMsg] = useState('');
  const nav = useNavigate();

  const handle = async (e) => {
    e.preventDefault();
    try {
      await axios.post('http://localhost:8080/api/auth/register', form);
      setMsg('Registered! Redirecting...');
      setTimeout(() => nav('/login'), 1500);
    } catch {
      setMsg('Error registering. Try again.');
    }
  };

  return (
    <div style={{ minHeight: '100vh', background: '#f0f4f8', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <div style={{ background: '#fff', borderRadius: 16, padding: '48px 40px', width: 480, boxShadow: '0 4px 24px rgba(0,0,0,0.08)' }}>
        <div style={{ fontSize: 24, fontWeight: 700, marginBottom: 6 }}>Create Account</div>
        <div style={{ color: '#64748b', fontSize: 14, marginBottom: 32 }}>Join MediCore HMS</div>

        {msg && <div style={{ background: '#f0fdf4', color: '#16a34a', padding: '10px 14px', borderRadius: 8, fontSize: 13, marginBottom: 16 }}>{msg}</div>}

        <form onSubmit={handle}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0 16px' }}>
            {[['username','Username','text'],['email','Email','email'],['password','Password','password']].map(([k,label,type]) => (
              <div key={k} style={{ gridColumn: k === 'password' ? '1 / -1' : 'auto' }}>
                <div style={{ fontSize: 13, fontWeight: 600, color: '#374151', marginBottom: 6 }}>{label}</div>
                <input type={type} value={form[k]} onChange={e => setForm({...form,[k]:e.target.value})}
                  style={{ width: '100%', padding: '11px 14px', border: '1.5px solid #e2e8f0', borderRadius: 8, fontSize: 14, outline: 'none', marginBottom: 16 }} />
              </div>
            ))}
            <div style={{ gridColumn: '1 / -1' }}>
              <div style={{ fontSize: 13, fontWeight: 600, color: '#374151', marginBottom: 6 }}>Role</div>
              <select value={form.role} onChange={e => setForm({...form,role:e.target.value})}
                style={{ width: '100%', padding: '11px 14px', border: '1.5px solid #e2e8f0', borderRadius: 8, fontSize: 14, outline: 'none', marginBottom: 20 }}>
                {['DOCTOR','NURSE','ADMIN','RECEPTIONIST'].map(r => <option key={r}>{r}</option>)}
              </select>
            </div>
          </div>
          <button type="submit" style={{ width: '100%', padding: 13, background: '#0ea5e9', color: '#fff', border: 'none', borderRadius: 8, fontSize: 15, fontWeight: 600, cursor: 'pointer' }}>
            Create Account
          </button>
        </form>
        <div style={{ marginTop: 20, textAlign: 'center', fontSize: 13, color: '#64748b' }}>
          Already have an account?{' '}
          <span onClick={() => nav('/login')} style={{ color: '#0ea5e9', cursor: 'pointer', fontWeight: 600 }}>Sign in</span>
        </div>
      </div>
    </div>
  );
}