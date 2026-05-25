import axios from 'axios';

const API = axios.create({
  baseURL: process.env.NODE_ENV === 'production'
    ? '/api'
    : 'http://localhost:8080/api'
});

export const getBeds          = ()     => API.get('/beds');
export const getBedStats      = ()     => API.get('/beds/stats');
export const getPatients      = ()     => API.get('/patients');
export const admitPatient     = (data) => API.post('/patients', data);
export const dischargePatient = (id)   => API.put(`/patients/${id}/discharge`);