package com.medicore.service;

import com.medicore.model.Patient;
import com.medicore.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient savePatient(Patient p) {
        return patientRepository.save(p);
    }

    public Patient dischargePatient(Long id) {
        Patient p = patientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        p.setStatus("Discharged");
        p.setExitDate(LocalDate.now());
        return patientRepository.save(p);
    }
}
