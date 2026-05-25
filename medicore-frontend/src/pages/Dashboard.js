import React, { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { getBedStats, getPatients } from '../services/api';

export default function Dashboard({ user, onLogout }) {
  const [stats, setStats] = useState({ total: 0, available: 0, occupied: 0, maintenance: 0 });
  const [patients, setPatients] = useState([]);

  const hour = new Date().getHours();
  const greet = hour < 12 ? 'Good Morning' : hour < 17 ? 'Good Afternoon' : 'Good Evening';

  useEffect(() => {
    getBedStats()
      .then(r => setStats(r.data))
      .catch(() => setStats({ total: 20, available: 12, occupied: 7, maintenance: 1 }));
    getPatients()
      .then(r => setPatients(r.data.slice(0, 5)))
      .catch(() => setPatients([]));
  }, []);

  const cards = [
    { label: 'Total Beds',   value: stats.total,       color: '#6366f1', icon: '🛏️' },
    { label: 'Available',    value: stats.available,   color: '#10b981', icon: '✅' },
    { label: 'Occupied',     value: stats.occupied,    color: '#f59e0b', icon: '🔴' },
    { label: 'Maintenance',  value: stats.maintenance, color: '#ef4444', icon: '🔧' },
  ];

  return (
    <Layout onLogout={onLogout}>
      <div style={{ marginBottom: 32 }}>
        <div style={{ fontSize: 26, fontWeight: 700 }}>{greet}, {user.username} 👋</div>
        <div style={{ color: '#64748b', fontSize: 14, marginTop: 4 }}>
          {new Date().toDateString()} · Hospital Overview
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 20, marginBottom: 32 }}>
        {cards.map(c => (
          <div key={c.label} style={{
            background: '#fff', borderRadius: 14, padding: 24,
            boxShadow: '0 1px 4px rgba(0,0,0,0.07)', borderTop: `4px solid ${c.color}`
          }}>
            <div style={{ fontSize: 28 }}>{c.icon}</div>
            <div style={{ fontSize: 36, fontWeight: 800, color: c.color, margin: '10px 0 4px' }}>{c.value}</div>
            <div style={{ fontSize: 13, color: '#64748b' }}>{c.label}</div>
          </div>
        ))}
      </div>

      <div style={{ background: '#fff', borderRadius: 14, padding: 28, boxShadow: '0 1px 4px rgba(0,0,0,0.07)' }}>
        <div style={{ fontSize: 16, fontWeight: 700, marginBottom: 20 }}>Recent Patients</div>
        {patients.length === 0
          ? <div style={{ color: '#94a3b8', textAlign: 'center', padding: 40 }}>
              No patients yet — admit one from the Patients page
            </div>
          : <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  {['Name', 'Age', 'Ward', 'Status', 'Admitted'].map(h => (
                    <th key={h} style={{
                      padding: '10px 14px', textAlign: 'left',
                      fontSize: 12, color: '#64748b', fontWeight: 600,
                      borderBottom: '1px solid #e2e8f0'
                    }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {patients.map(p => (
                  <tr key={p.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                    <td style={{ padding: '12px 14px', fontWeight: 500 }}>{p.name}</td>
                    <td style={{ padding: '12px 14px', color: '#64748b' }}>{p.age}</td>
                    <td style={{ padding: '12px 14px', color: '#64748b' }}>{p.ward}</td>
                    <td style={{ padding: '12px 14px' }}>
                      <span style={{
                        background: (p.status === 'Admitted' || p.status === 'ADMITTED') ? '#dcfce7' : '#f1f5f9',
                        color: (p.status === 'Admitted' || p.status === 'ADMITTED') ? '#16a34a' : '#64748b',
                        padding: '3px 10px', borderRadius: 20, fontSize: 12, fontWeight: 600
                      }}>{p.status}</span>
                    </td>
                    <td style={{ padding: '12px 14px', color: '#94a3b8', fontSize: 13 }}>
                      {p.entryDate || p.admissionDate || '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
        }
      </div>
    </Layout>
  );
}