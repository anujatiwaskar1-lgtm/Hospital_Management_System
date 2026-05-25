import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Patients from './pages/Patients';
import Beds from './pages/Beds';

export default function App() {
  const [user, setUser] = useState(null);

  return (
    <Router>
      <Routes>
        <Route path="/"          element={<Navigate to="/login" />} />
        <Route path="/login"     element={user ? <Navigate to="/dashboard" /> : <Login onLogin={setUser} />} />
        <Route path="/register"  element={<Register />} />
        <Route path="/dashboard" element={user ? <Dashboard user={user} onLogout={() => setUser(null)} /> : <Navigate to="/login" />} />
        <Route path="/patients"  element={user ? <Patients  user={user} onLogout={() => setUser(null)} /> : <Navigate to="/login" />} />
        <Route path="/beds"      element={user ? <Beds      user={user} onLogout={() => setUser(null)} /> : <Navigate to="/login" />} />
      </Routes>
    </Router>
  );
}