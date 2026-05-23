package com.medicore.repository;

import com.medicore.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ✅ PatientRepository — Spring Data JPA
 *
 * Spring auto-generates SQL for all methods below at runtime.
 * NO SQL code needed — method names tell Spring what query to build.
 *
 * Extends JpaRepository<Patient, Long> which gives free:
 *   save(), findById(), findAll(), delete(), count(), existsById(), etc.
 *
 * These map directly to MySQL `patients` table via Hibernate.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * SELECT * FROM patients WHERE status = ?
     * Used for "Admitted" and "Discharged" filters.
     */
    List<Patient> findByStatus(String status);

    /**
     * SELECT * FROM patients WHERE patient_id = ?
     * Used when discharging a specific patient.
     */
    Optional<Patient> findByPatientId(String patientId);

    /**
     * SELECT * FROM patients WHERE status = ? ORDER BY entry_date DESC
     */
    List<Patient> findByStatusOrderByEntryDateDesc(String status);

    /**
     * SELECT * FROM patients
     * WHERE LOWER(name) LIKE %search%
     *    OR LOWER(patient_id) LIKE %search%
     *
     * Used by the search box in Patient Records.
     */
    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.patientId) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Patient> searchByNameOrId(@Param("search") String search);

    /**
     * SELECT COUNT(*) FROM patients WHERE status='Discharged' AND exit_date = TODAY
     * Used for "Discharged Today" stat card.
     */
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.status = 'Discharged' AND p.exitDate = :today")
    long countDischargedToday(@Param("today") LocalDate today);

    /**
     * SELECT COUNT(*) FROM patients WHERE entry_date = :date
     * Used to build the 7-day admissions chart.
     */
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.entryDate = :date")
    long countAdmissionsOnDate(@Param("date") LocalDate date);

    /**
     * SELECT ward, COUNT(*) FROM patients WHERE status='Admitted' GROUP BY ward
     * Returns Object[] pairs: [ward, count]
     */
    @Query("SELECT p.ward, COUNT(p) FROM Patient p WHERE p.status = 'Admitted' GROUP BY p.ward")
    List<Object[]> countAdmittedByWard();

    /**
     * SELECT AVG(age) FROM patients WHERE status = 'Admitted'
     */
    @Query("SELECT AVG(p.age) FROM Patient p WHERE p.status = 'Admitted'")
    Double averageAgeOfAdmitted();
}
