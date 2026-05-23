package com.medicore.service;

import com.medicore.model.Bed;
import com.medicore.model.Patient;
import com.medicore.repository.BedRepository;
import com.medicore.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * ✅ PatientService — Spring @Service layer
 *
 * This is the BRIDGE between JavaFX UI and MySQL database.
 *
 * HOW IT CONNECTS:
 *   JavaFX Screen
 *       ↓  calls Java method (no JSON, no HTTP)
 *   PatientService   ← Spring manages this bean
 *       ↓  calls repository methods
 *   PatientRepository / BedRepository   ← JPA interfaces
 *       ↓  Hibernate generates SQL
 *   MySQL Database
 *
 * @Transactional ensures each operation is atomic —
 * if something fails, the whole operation is rolled back.
 */
@Service
@Transactional
public class PatientService {

    // ✅ Spring auto-wires these — no new() needed
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private BedRepository bedRepository;

    // ─────────────────────────────────────────────────────────────────────────
    //  ADMIT PATIENT
    //  Called by: DashboardScreen "Admit Patient" button
    //  Saves new Patient row to MySQL AND marks the bed as OCCUPIED
    // ─────────────────────────────────────────────────────────────────────────
    public Patient admitPatient(String patientId, String name, int age,
                                String ward, String bedId, String diagnosis) {

        // 1. Verify bed exists in DB
        Bed bed = bedRepository.findByBedId(bedId)
                .orElseThrow(() -> new RuntimeException("Bed not found: " + bedId));

        // 2. Verify bed is free
        if (!bed.isAvailable()) {
            throw new RuntimeException("Bed " + bedId + " is currently OCCUPIED.");
        }

        // 3. INSERT new patient into MySQL
        Patient patient = new Patient(patientId, name, age, ward, bedId, diagnosis);
        patient = patientRepository.save(patient);  // Hibernate: INSERT INTO patients ...

        // 4. UPDATE bed status in MySQL
        bed.setStatus("OCCUPIED");
        bed.setCurrentPatientId(patientId);
        bedRepository.save(bed);  // Hibernate: UPDATE beds SET status='OCCUPIED' ...

        return patient;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DISCHARGE PATIENT
    //  Called by: Patient Records table "Discharge" button
    //  Updates patient status + exit date AND frees the bed
    // ─────────────────────────────────────────────────────────────────────────
    public Patient dischargePatient(String patientId) {

        // 1. Find patient in MySQL
        Patient patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));

        if ("Discharged".equals(patient.getStatus())) {
            throw new RuntimeException("Patient " + patientId + " is already discharged.");
        }

        // 2. UPDATE patient row in MySQL
        patient.setStatus("Discharged");
        patient.setExitDate(LocalDate.now());
        patient = patientRepository.save(patient);  // Hibernate: UPDATE patients SET status=...

        // 3. Free the bed — UPDATE beds table
        bedRepository.findByBedId(patient.getBedId()).ifPresent(bed -> {
            bed.setStatus("AVAILABLE");
            bed.setCurrentPatientId(null);
            bedRepository.save(bed);  // Hibernate: UPDATE beds SET status='AVAILABLE'...
        });

        return patient;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  READ OPERATIONS (read-only transactions for performance)
    // ─────────────────────────────────────────────────────────────────────────

    /** SELECT * FROM patients */
    @Transactional(readOnly = true)
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    /** SELECT * FROM patients WHERE status = 'Admitted' */
    @Transactional(readOnly = true)
    public List<Patient> getAdmittedPatients() {
        return patientRepository.findByStatus("Admitted");
    }

    /** SELECT * FROM patients WHERE status = 'Discharged' */
    @Transactional(readOnly = true)
    public List<Patient> getDischargedPatients() {
        return patientRepository.findByStatus("Discharged");
    }

    /** Full-text search on name or patient_id */
    @Transactional(readOnly = true)
    public List<Patient> searchPatients(String query) {
        if (query == null || query.isBlank()) {
            return patientRepository.findAll();
        }
        return patientRepository.searchByNameOrId(query.trim());
    }

    /** Count of patients discharged today (for stat card) */
    @Transactional(readOnly = true)
    public long countDischargedToday() {
        return patientRepository.countDischargedToday(LocalDate.now());
    }

    /** Admissions count for a specific date (for 7-day chart) */
    @Transactional(readOnly = true)
    public long countAdmissionsOnDate(LocalDate date) {
        return patientRepository.countAdmissionsOnDate(date);
    }

    /** Ward-wise admitted count: [[ward, count], ...] */
    @Transactional(readOnly = true)
    public List<Object[]> countAdmittedByWard() {
        return patientRepository.countAdmittedByWard();
    }

    /** Average age of admitted patients */
    @Transactional(readOnly = true)
    public double averageAgeOfAdmitted() {
        Double avg = patientRepository.averageAgeOfAdmitted();
        return avg != null ? avg : 0.0;
    }

    /** Auto-generate next Patient ID like P-0043 */
    @Transactional(readOnly = true)
    public String generateNextPatientId() {
        long total = patientRepository.count();
        return String.format("P-%04d", total + 1);
    }
}
