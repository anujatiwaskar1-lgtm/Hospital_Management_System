package com.medicore.controller;

import com.medicore.model.Bed;
import com.medicore.service.BedService;
import com.medicore.repository.BedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/beds")
@CrossOrigin(origins = "*")
public class BedController {

    @Autowired
    private BedService bedService;

    @Autowired
    private BedRepository bedRepository;

    @GetMapping
    public List<Bed> getAllBeds() {
        return bedService.getAllBeds();
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        List<Bed> beds = bedRepository.findAll();
        Map<String, Long> stats = new HashMap<>();
        stats.put("total",       (long) beds.size());
        stats.put("available",   beds.stream().filter(b -> "AVAILABLE".equals(b.getStatus())).count());
        stats.put("occupied",    beds.stream().filter(b -> "OCCUPIED".equals(b.getStatus())).count());
        stats.put("maintenance", beds.stream().filter(b -> "MAINTENANCE".equals(b.getStatus())).count());
        return stats;
    }
}