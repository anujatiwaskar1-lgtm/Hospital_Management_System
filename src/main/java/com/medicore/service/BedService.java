package com.medicore.service;

import com.medicore.model.Bed;
import com.medicore.repository.BedRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BedService {

    @Autowired
    private BedRepository bedRepository;

    @PostConstruct
    public void initializeBedsIfEmpty() {
        if (bedRepository.count() == 0) {
            String[][] beds = {
                {"B-01","General"},{"B-02","General"},{"B-03","General"},{"B-04","General"},{"B-05","General"},
                {"B-06","ICU"},{"B-07","ICU"},{"B-08","ICU"},{"B-09","ICU"},{"B-10","ICU"},
                {"B-11","Pediatric"},{"B-12","Pediatric"},{"B-13","Pediatric"},{"B-14","Pediatric"},{"B-15","Pediatric"},
                {"B-16","Orthopedic"},{"B-17","Orthopedic"},{"B-18","Orthopedic"},{"B-19","Orthopedic"},{"B-20","Orthopedic"}
            };
            for (String[] b : beds) {
                bedRepository.save(new Bed(b[0], b[1]));
            }
        }
    }

    public List<Bed> getAllBeds() {
        return bedRepository.findAll();
    }
}