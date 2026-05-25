import React, { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { getBeds } from '../services/api';

const STATUS_BG   = { AVAILABLE: '#dcfce7', OCCUPIED: '#fef9c3', MAINTENANCE: '#fee2e2' };
const STATUS_TEXT = { AVAILABLE: '#15803d', OCCUPIED: '#92400e', MAINTENANCE: '#dc2626' };

export default function Beds({ user, onLogout }) {
  const [beds, setBeds]     = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [lastUpdated, setLastUpdated] = useState(new Date());

  const load = () => {
    getBeds()
      .then(r => { setBeds(r.data); setLastUpdated(new Date()); })
      .catch(() => {});
  };

  useEffect(() => {
    load();
    // Auto-refresh every 10 seconds
    const interval = setInterval(load, 10000);
    return () => clearInterval(interval);
  }, []);

  const filtered  = filter === 'ALL' ? beds : beds.filter(b => b.status === filter);
  const available = beds.filter(b => b.status === 'AVAILABLE').length;
  const occupied  = beds.filter(b => b.status === 'OCCUPIED').length;
  const maintenance = beds.filter(b => b.status === 'MAINTENANCE').length;
  const pct = beds.length ? Math.round(occupied / beds.length * 100) : 0;

  return (
    <Layout onLogout={onLogout}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 28 }}>
        <div>
          <div style={{ fontSize: 24, fontWeight: 700 }}>Bed Map</div>
          <div style={{ color: '#64748b', fontSize: 14 }}>{available} of {beds.length} beds available</div>
        </div>
        <button onClick={load} style={{ background: '#f1f5f9', color: '#374151', border: '1px solid #e2e8f0', borderRadius: 8, padding: '8px 16px', cursor: 'pointer', fontSize: 13, fontWeight: 600 }}>
          🔄 Refresh
        </button>
      </div>

      {/* Filter tabs */}
      <div style={{ background: '#fff', borderRadius: 10, padding: 5, display: 'inline-flex', gap: 4, marginBottom: 24 }}>
        {['ALL','AVAILABLE','OCCUPIED','MAINTENANCE'].map(f => (
          <button key={f} onClick={() => setFilter(f)} style={{
            padding: '8px 18px', border: 'none', borderRadius: 7, cursor: 'pointer',
            fontWeight: 600, fontSize: 13,
            background: filter===f ? '#0ea5e9' : 'transparent',
            color: filter===f ? '#fff' : '#64748b'
          }}>{f}</button>
        ))}
      </div>

      {/* Occupancy bar */}
      <div style={{ background: '#fff', borderRadius: 14, padding: '20px 24px', marginBottom: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.07)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 10 }}>
          <span style={{ fontSize: 13, fontWeight: 600 }}>Occupancy Rate</span>
          <span style={{ fontSize: 13, fontWeight: 700, color: pct > 80 ? '#ef4444' : '#10b981' }}>{pct}%</span>
        </div>
        <div style={{ background: '#f1f5f9', borderRadius: 99, height: 10 }}>
          <div style={{ width: `${pct}%`, background: pct > 80 ? '#ef4444' : '#10b981', height: 10, borderRadius: 99, transition: 'width 0.6s' }} />
        </div>
        <div style={{ display: 'flex', gap: 24, marginTop: 12 }}>
          <span style={{ fontSize: 12, color: '#64748b' }}>🟢 Available: {available}</span>
          <span style={{ fontSize: 12, color: '#64748b' }}>🟡 Occupied: {occupied}</span>
          <span style={{ fontSize: 12, color: '#64748b' }}>🔴 Maintenance: {maintenance}</span>
          <span style={{ fontSize: 12, color: '#94a3b8', marginLeft: 'auto' }}>
            Last updated: {lastUpdated.toLocaleTimeString()}
          </span>
        </div>
      </div>

      {/* Bed grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))', gap: 14 }}>
        {filtered.map(bed => (
          <div key={bed.id} style={{
            background: STATUS_BG[bed.status] || '#f8fafc',
            border: `1.5px solid ${STATUS_TEXT[bed.status] || '#e2e8f0'}33`,
            borderRadius: 12, padding: '18px 16px',
            transition: 'all 0.3s'
          }}>
            {/* Use bedId field from your Bed.java */}
            <div style={{ fontSize: 16, fontWeight: 800, color: '#0f172a' }}>{bed.bedId}</div>
            <div style={{ fontSize: 12, color: '#64748b', margin: '4px 0 10px' }}>{bed.ward}</div>
            <span style={{
              background: STATUS_BG[bed.status],
              color: STATUS_TEXT[bed.status],
              padding: '2px 8px', borderRadius: 20, fontSize: 11, fontWeight: 700
            }}>{bed.status}</span>
            {bed.currentPatientId && (
              <div style={{ fontSize: 11, color: '#64748b', marginTop: 8 }}>
                👤 {bed.currentPatientId}
              </div>
            )}
          </div>
        ))}
      </div>
    </Layout>
  );
}