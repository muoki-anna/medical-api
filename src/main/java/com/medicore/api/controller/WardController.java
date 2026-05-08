package com.medicore.api.controller;

import com.medicore.api.model.Ward;
import com.medicore.api.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class WardController {

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private com.medicore.api.repository.PatientRepository patientRepository;

    @GetMapping("/wards")
    public ResponseEntity<?> getWards() {
        List<Ward> wards = wardRepository.findAll();
        List<com.medicore.api.model.Patient> allPatients = patientRepository.findAll();
        
        // Enhance with real occupancy from the patient index
        List<Map<String, Object>> enhancedWards = wards.stream().map(w -> {
            Map<String, Object> map = new HashMap<>();
            String name = w.getName() != null ? w.getName() : "Ward";
            int capacity = w.getCapacity() != null ? w.getCapacity() : 20;
            
            // Calculate real occupancy
            long occupied = allPatients.stream()
                    .filter(p -> p.getWard() != null && p.getWard().getId().equals(w.getId()))
                    .count();
            int available = (int) (capacity - occupied);

            map.put("id", w.getId());
            map.put("name", name);
            map.put("totalBeds", capacity);
            map.put("occupied", occupied);
            map.put("available", Math.max(0, available));
            
            // Mock beds
            List<Map<String, String>> beds = new java.util.ArrayList<>();
            char prefix = name.length() > 0 ? name.charAt(name.length()-1) : 'U';
            for (int i = 1; i <= capacity; i++) {
                Map<String, String> bed = new HashMap<>();
                bed.put("number", prefix + String.format("%02d", i));
                bed.put("status", i <= occupied ? "occupied" : "available");
                beds.add(bed);
            }
            map.put("beds", beds);
            return map;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", enhancedWards);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/wards")
    public ResponseEntity<?> saveWard(@RequestBody Ward ward) {
        if (ward.getCapacity() == null) ward.setCapacity(20);
        if (ward.getOccupied() == null) ward.setOccupied(0);
        if (ward.getAvailable() == null) ward.setAvailable(ward.getCapacity());
        
        wardRepository.save(ward);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Ward saved successfully"));
    }

    @DeleteMapping("/wards")
    public ResponseEntity<?> deleteWard(@RequestParam Long id) {
        wardRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Ward deleted successfully"));
    }
}
