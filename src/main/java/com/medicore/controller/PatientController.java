package com.medicore.controller;

import com.medicore.model.Patient;
import com.medicore.model.Bed;
import com.medicore.service.PatientService;
import com.medicore.repository.BedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private BedRepository bedRepository;

    @GetMapping
    public List<Patient> getAllPatients() {
        return patientService.getAllPatients();
    }

    @PostMapping
    public Patient admitPatient(@RequestBody Map<String, String> body) {
        String ward = body.getOrDefault("ward", "General");

        // Find first available bed in that ward
        Optional<Bed> availableBed = bedRepository.findAll()
            .stream()
            .filter(b -> b.getWard().equalsIgnoreCase(ward) && "AVAILABLE".equals(b.getStatus()))
            .findFirst();

        String assignedBedId = "NONE";
        if (availableBed.isPresent()) {
            Bed bed = availableBed.get();
            assignedBedId = bed.getBedId();
            bed.setStatus("OCCUPIED");
            bed.setCurrentPatientId("P-" + System.currentTimeMillis() % 100000);
            bedRepository.save(bed);
        }

        Patient p = new Patient();
        p.setPatientId("P-" + System.currentTimeMillis() % 100000);
        p.setName(body.get("name"));
        p.setAge(Integer.parseInt(body.getOrDefault("age", "0")));
        p.setWard(ward);
        p.setBedId(assignedBedId);
        p.setDiagnosis(body.getOrDefault("diagnosis", ""));
        p.setEntryDate(LocalDate.now());
        p.setStatus("Admitted");
        return patientService.savePatient(p);
    }

    @PutMapping("/{id}/discharge")
    public Patient discharge(@PathVariable Long id) {
        Patient p = patientService.dischargePatient(id);

        // Free the bed this patient was using
        bedRepository.findAll()
            .stream()
            .filter(b -> b.getBedId().equals(p.getBedId()))
            .findFirst()
            .ifPresent(bed -> {
                bed.setStatus("AVAILABLE");
                bed.setCurrentPatientId(null);
                bedRepository.save(bed);
            });

        return p;
    }
}