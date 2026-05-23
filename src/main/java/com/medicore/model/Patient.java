package com.medicore.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ✅ Patient Entity — JPA maps this class directly to MySQL table `patients`
 *
 * Hibernate auto-creates this table on first run (ddl-auto=update in application.properties):
 *
 *   CREATE TABLE patients (
 *     id             BIGINT AUTO_INCREMENT PRIMARY KEY,
 *     patient_id     VARCHAR(20) UNIQUE NOT NULL,
 *     name           VARCHAR(100) NOT NULL,
 *     age            INT NOT NULL,
 *     ward           VARCHAR(50) NOT NULL,
 *     bed_id         VARCHAR(10) NOT NULL,
 *     diagnosis      VARCHAR(200) NOT NULL,
 *     entry_date     DATE NOT NULL,
 *     exit_date      DATE,
 *     status         VARCHAR(20) NOT NULL
 *   );
 */
@Entity
@Table(name = "patients")
public class Patient {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─── Primary Key ──────────────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Columns ──────────────────────────────────────────────────────────────
    @Column(name = "patient_id", unique = true, nullable = false, length = 20)
    private String patientId;   // e.g. "P-0042"

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, length = 50)
    private String ward;        // General | ICU | Pediatric | Orthopedic

    @Column(name = "bed_id", nullable = false, length = 10)
    private String bedId;       // e.g. "B-03"

    @Column(nullable = false, length = 200)
    private String diagnosis;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "exit_date")
    private LocalDate exitDate;  // null until discharged

    @Column(nullable = false, length = 20)
    private String status;       // "Admitted" | "Discharged"

    // ─── Constructors ─────────────────────────────────────────────────────────
    public Patient() {}

    public Patient(String patientId, String name, int age,
                   String ward, String bedId, String diagnosis) {
        this.patientId = patientId;
        this.name      = name;
        this.age       = age;
        this.ward      = ward;
        this.bedId     = bedId;
        this.diagnosis = diagnosis;
        this.entryDate = LocalDate.now();
        this.status    = "Admitted";
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────
    public Long getId()                     { return id; }
    public void setId(Long id)              { this.id = id; }

    public String getPatientId()            { return patientId; }
    public void setPatientId(String v)      { this.patientId = v; }

    public String getName()                 { return name; }
    public void setName(String v)           { this.name = v; }

    public int getAge()                     { return age; }
    public void setAge(int v)               { this.age = v; }

    public String getWard()                 { return ward; }
    public void setWard(String v)           { this.ward = v; }

    public String getBedId()               { return bedId; }
    public void setBedId(String v)         { this.bedId = v; }

    public String getDiagnosis()           { return diagnosis; }
    public void setDiagnosis(String v)     { this.diagnosis = v; }

    public LocalDate getEntryDate()        { return entryDate; }
    public void setEntryDate(LocalDate v)  { this.entryDate = v; }

    /** Formatted string for JavaFX TableView display */
    public String getEntryDateStr() {
        return entryDate != null ? entryDate.format(FMT) : "";
    }

    public LocalDate getExitDate()         { return exitDate; }
    public void setExitDate(LocalDate v)   { this.exitDate = v; }

    /** Formatted string for JavaFX TableView display */
    public String getExitDateStr() {
        return exitDate != null ? exitDate.format(FMT) : "-";
    }

    public String getStatus()              { return status; }
    public void setStatus(String v)        { this.status = v; }

    @Override
    public String toString() {
        return "Patient{" + patientId + ", " + name + ", " + ward + ", " + status + "}";
    }
}
