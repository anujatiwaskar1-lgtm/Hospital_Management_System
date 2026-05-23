package com.medicore.service;

import com.medicore.model.Bed;
import com.medicore.repository.BedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ✅ BedService — Spring @Service for bed management
 *
 * Called directly by JavaFX DashboardScreen (no JSON/HTTP).
 * All operations touch MySQL via BedRepository → Hibernate.
 */
@Service
@Transactional
public class BedService {

    @Autowired
    private BedRepository bedRepository;

    // ─────────────────────────────────────────────────────────────────────────
    //  SEED BEDS (First-Run Only)
    //  Called from MediCoreApp.init() after Spring starts.
    //  If the `beds` table is empty, inserts 20 beds into MySQL.
    // ─────────────────────────────────────────────────────────────────────────
    public void initializeBedsIfEmpty() {
        if (bedRepository.count() == 0) {
            String[][] bedConfig = {
                {"General",    "B-01", "B-02", "B-03", "B-04", "B-05"},
                {"ICU",        "B-06", "B-07", "B-08", "B-09", "B-10"},
                {"Pediatric",  "B-11", "B-12", "B-13", "B-14", "B-15"},
                {"Orthopedic", "B-16", "B-17", "B-18", "B-19", "B-20"}
            };

            for (String[] wardRow : bedConfig) {
                String ward = wardRow[0];
                for (int i = 1; i < wardRow.length; i++) {
                    bedRepository.save(new Bed(wardRow[i], ward));
                    // Hibernate: INSERT INTO beds (bed_id, ward, status) VALUES (...)
                }
            }
            System.out.println("✅ BedService: 20 beds seeded into MySQL.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  READ OPERATIONS
    // ─────────────────────────────────────────────────────────────────────────

    /** SELECT * FROM beds ORDER BY bed_id — used to render Bed Map */
    @Transactional(readOnly = true)
    public List<Bed> getAllBeds() {
        return bedRepository.findAllByOrderByBedIdAsc();
    }

    /** SELECT * FROM beds WHERE status = 'AVAILABLE' */
    @Transactional(readOnly = true)
    public List<Bed> getAvailableBeds() {
        return bedRepository.findByStatus("AVAILABLE");
    }

    /** SELECT * FROM beds WHERE ward = ? ORDER BY bed_id */
    @Transactional(readOnly = true)
    public List<Bed> getBedsByWard(String ward) {
        return bedRepository.findByWardOrderByBedIdAsc(ward);
    }

    /** SELECT COUNT(*) FROM beds WHERE status = 'AVAILABLE' */
    @Transactional(readOnly = true)
    public long countAvailable() {
        return bedRepository.countByStatus("AVAILABLE");
    }

    /** SELECT COUNT(*) FROM beds WHERE status = 'OCCUPIED' */
    @Transactional(readOnly = true)
    public long countOccupied() {
        return bedRepository.countByStatus("OCCUPIED");
    }

    /** SELECT COUNT(*) FROM beds */
    @Transactional(readOnly = true)
    public long totalBeds() {
        return bedRepository.count();
    }
}
