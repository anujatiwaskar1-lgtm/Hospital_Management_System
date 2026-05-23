package com.medicore.model;

import jakarta.persistence.*;

/**
 * ✅ Bed Entity — JPA maps this class directly to MySQL table `beds`
 *
 * Auto-created table structure:
 *
 *   CREATE TABLE beds (
 *     id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
 *     bed_id              VARCHAR(10) UNIQUE NOT NULL,
 *     ward                VARCHAR(50) NOT NULL,
 *     status              VARCHAR(20) NOT NULL,   -- AVAILABLE | OCCUPIED
 *     current_patient_id  VARCHAR(20)             -- null if available
 *   );
 *
 * 20 beds are seeded by BedService.initializeBedsIfEmpty() on first launch:
 *   B-01 to B-05  → General
 *   B-06 to B-10  → ICU
 *   B-11 to B-15  → Pediatric
 *   B-16 to B-20  → Orthopedic
 */
@Entity
@Table(name = "beds")
public class Bed {

    // ─── Primary Key ──────────────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Columns ──────────────────────────────────────────────────────────────
    @Column(name = "bed_id", unique = true, nullable = false, length = 10)
    private String bedId;          // e.g. "B-01"

    @Column(nullable = false, length = 50)
    private String ward;           // General | ICU | Pediatric | Orthopedic

    @Column(nullable = false, length = 20)
    private String status;         // "AVAILABLE" | "OCCUPIED"

    @Column(name = "current_patient_id", length = 20)
    private String currentPatientId;  // null when bed is free

    // ─── Constructors ─────────────────────────────────────────────────────────
    public Bed() {}

    public Bed(String bedId, String ward) {
        this.bedId  = bedId;
        this.ward   = ward;
        this.status = "AVAILABLE";
    }

    // ─── Helper ───────────────────────────────────────────────────────────────
    public boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────
    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }

    public String getBedId()                    { return bedId; }
    public void setBedId(String v)              { this.bedId = v; }

    public String getWard()                     { return ward; }
    public void setWard(String v)               { this.ward = v; }

    public String getStatus()                   { return status; }
    public void setStatus(String v)             { this.status = v; }

    public String getCurrentPatientId()         { return currentPatientId; }
    public void setCurrentPatientId(String v)   { this.currentPatientId = v; }

    @Override
    public String toString() {
        return "Bed{" + bedId + ", " + ward + ", " + status + "}";
    }
}
