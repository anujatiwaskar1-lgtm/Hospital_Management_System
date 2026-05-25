import React, { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { getPatients, admitPatient, dischargePatient, getBeds } from '../services/api';

const inp = { width: '100%', padding: '10px 14px', border: '1.5px solid #e2e8f0', borderRadius: 8, fontSize: 14, marginBottom: 14, outline: 'none', background: '#fff' };

export default function Patients({ user, onLogout }) {
  const [patients, setPatients] = useState([]);
  const [search, setSearch]     = useState('');
  const [showForm, setShowForm] = useState(false);
  const [beds, setBeds]         = useState([]);
  const [availableBeds, setAvailableBeds] = useState([]);
  const [form, setForm]         = useState({ name:'', age:'', ward:'', bedId:'', diagnosis:'' });

  const load = () => getPatients().then(r => setPatients(r.data)).catch(() => {});

  useEffect(() => { load(); }, []);

  // Load all beds once when form opens
  useEffect(() => {
    if (showForm) {
      getBeds().then(r => setBeds(r.data)).catch(() => {});
    }
  }, [showForm]);

  // Filter available beds when ward changes
  useEffect(() => {
    if (form.ward) {
      const filtered = beds.filter(
        b => b.ward.toLowerCase() === form.ward.toLowerCase() && b.status === 'AVAILABLE'
      );
      setAvailableBeds(filtered);
      setForm(f => ({ ...f, bedId: filtered[0]?.bedId || '' }));
    } else {
      setAvailableBeds([]);
    }
  }, [form.ward, beds]);

  const admit = async () => {
    try {
      await admitPatient(form);
      load();
      setShowForm(false);
      setForm({ name:'', age:'', ward:'', bedId:'', diagnosis:'' });
      setBeds([]);
      setAvailableBeds([]);
    } catch {
      alert('Error — check backend is running on port 8080');
    }
  };

  const discharge = async (id) => {
    if (!window.confirm('Discharge this patient?')) return;
    try { await dischargePatient(id); load(); }
    catch { alert('Error discharging'); }
  };

  const filtered = patients.filter(p => p.name?.toLowerCase().includes(search.toLowerCase()));
  const canSubmit = form.name && form.age && form.ward && form.bedId && form.diagnosis;

  return (
    <Layout onLogout={onLogout}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 28 }}>
        <div>
          <div style={{ fontSize: 24, fontWeight: 700 }}>Patients</div>
          <div style={{ color: '#64748b', fontSize: 14 }}>{patients.length} total</div>
        </div>
        <button onClick={() => setShowForm(!showForm)} style={{ background: showForm ? '#64748b' : '#0ea5e9', color: '#fff', border: 'none', borderRadius: 8, padding: '11px 22px', fontWeight: 600, cursor: 'pointer' }}>
          {showForm ? '✕ Cancel' : '+ Admit Patient'}
        </button>
      </div>

      {showForm && (
        <div style={{ background: '#fff', borderRadius: 14, padding: 28, marginBottom: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.07)' }}>
          <div style={{ fontSize: 16, fontWeight: 700, marginBottom: 20 }}>New Admission</div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0 20px' }}>

            {/* Name */}
            <div>
              <div style={{ fontSize: 13, fontWeight: 600, color: '#374151', marginBottom: 4 }}>Full Name</div>
              <input style={inp} value={form.name} onChange={e => setForm({...form, name: e.target.value})} placeholder="e.g. John Smith" />
            </div>

            {/* Age */}
            <div>
              <div style={{ fontSize: 13, fontWeight: 600, color: '#374151', marginBottom: 4 }}>Age</div>
              <input style={inp} type="number" value={form.age} onChange={e => setForm({...form, age: e.target.value})} placeholder="e.g. 35" />
            </div>

            {/* Ward Dropdown */}
            <div>
              <div style={{ fontSize: 13, fontWeight: 600, color: '#374151', marginBottom: 4 }}>Ward</div>
              <select
                style={{ ...inp, cursor: 'pointer' }}
                value={form.ward}
                onChange={e => setForm({...form, ward: e.target.value, bedId: ''})}>
                <option value="">-- Select Ward --</option>
                {['General','ICU','Pediatric','Orthopedic'].map(w => (
                  <option key={w} value={w}>{w}</option>
                ))}
              </select>
            </div>

            {/* Bed Dropdown */}
            <div>
              <div style={{ fontSize: 13, fontWeight: 600, color: '#374151', marginBottom: 4 }}>
                Bed Number
                {form.ward && (
                  <span style={{ marginLeft: 8, fontSize: 11, fontWeight: 600, color: availableBeds.length > 0 ? '#10b981' : '#ef4444' }}>
                    {availableBeds.length > 0 ? `${availableBeds.length} available` : 'No beds available'}
                  </span>
                )}
              </div>
              <select
                style={{ ...inp, cursor: form.ward ? 'pointer' : 'not-allowed', color: !form.ward || availableBeds.length === 0 ? '#94a3b8' : '#1e293b' }}
                value={form.bedId}
                onChange={e => setForm({...form, bedId: e.target.value})}
                disabled={!form.ward || availableBeds.length === 0}>
                {!form.ward && <option value="">Select ward first</option>}
                {form.ward && availableBeds.length === 0 && <option value="">No beds available in {form.ward}</option>}
                {availableBeds.map(b => (
                  <option key={b.bedId} value={b.bedId}>
                    {b.bedId} — {b.ward} Ward
                  </option>
                ))}
              </select>
            </div>

            {/* Diagnosis - full width */}
            <div style={{ gridColumn: '1 / -1' }}>
              <div style={{ fontSize: 13, fontWeight: 600, color: '#374151', marginBottom: 4 }}>Diagnosis</div>
              <input style={inp} value={form.diagnosis} onChange={e => setForm({...form, diagnosis: e.target.value})} placeholder="e.g. Fractured leg, Fever, etc." />
            </div>

          </div>

          <button
            onClick={admit}
            disabled={!canSubmit}
            style={{
              background: canSubmit ? '#10b981' : '#94a3b8',
              color: '#fff', border: 'none', borderRadius: 8,
              padding: '11px 28px', fontWeight: 600,
              cursor: canSubmit ? 'pointer' : 'not-allowed',
              marginTop: 4, fontSize: 14
            }}>
            ✓ Confirm Admission
          </button>
        </div>
      )}

      <div style={{ background: '#fff', borderRadius: 14, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.07)' }}>
        <input
          style={{ ...inp, maxWidth: 300, marginBottom: 20 }}
          placeholder="🔍  Search patients..."
          value={search}
          onChange={e => setSearch(e.target.value)} />

        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr>{['Patient','Age','Ward','Bed','Diagnosis','Status','Action'].map(h => (
              <th key={h} style={{ padding: '10px 14px', textAlign: 'left', fontSize: 12, color: '#64748b', fontWeight: 600, borderBottom: '1px solid #e2e8f0' }}>{h}</th>
            ))}</tr>
          </thead>
          <tbody>
            {filtered.map(p => (
              <tr key={p.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                <td style={{ padding: '13px 14px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <div style={{ width: 34, height: 34, borderRadius: '50%', background: '#e0f2fe', color: '#0284c7', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 13 }}>
                      {p.name?.charAt(0).toUpperCase()}
                    </div>
                    <span style={{ fontWeight: 500 }}>{p.name}</span>
                  </div>
                </td>
                <td style={{ padding: '13px 14px', color: '#64748b' }}>{p.age}</td>
                <td style={{ padding: '13px 14px', color: '#64748b' }}>{p.ward}</td>
                <td style={{ padding: '13px 14px' }}>
                  <span style={{ background: '#f1f5f9', color: '#374151', padding: '3px 10px', borderRadius: 6, fontSize: 12, fontWeight: 600 }}>{p.bedId}</span>
                </td>
                <td style={{ padding: '13px 14px', color: '#64748b' }}>{p.diagnosis}</td>
                <td style={{ padding: '13px 14px' }}>
                  <span style={{
                    background: (p.status==='Admitted'||p.status==='ADMITTED') ? '#dcfce7' : '#f1f5f9',
                    color: (p.status==='Admitted'||p.status==='ADMITTED') ? '#16a34a' : '#64748b',
                    padding: '3px 10px', borderRadius: 20, fontSize: 12, fontWeight: 600
                  }}>{p.status}</span>
                </td>
                <td style={{ padding: '13px 14px' }}>
                  {(p.status === 'Admitted' || p.status === 'ADMITTED') && (
                    <button onClick={() => discharge(p.id)} style={{ background: '#fef2f2', color: '#ef4444', border: '1px solid #fecaca', borderRadius: 6, padding: '5px 12px', cursor: 'pointer', fontSize: 12, fontWeight: 600 }}>
                      Discharge
                    </button>
                  )}
                </td>
              </tr>
            ))}
            {filtered.length === 0 && (
              <tr><td colSpan={7} style={{ padding: 40, textAlign: 'center', color: '#94a3b8' }}>No patients found</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}