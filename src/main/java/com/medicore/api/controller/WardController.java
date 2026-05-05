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

    @GetMapping("/wards")
    public ResponseEntity<?> getWards() {
        List<Ward> wards = wardRepository.findAll();
        
        // Enhance with mock beds for the UI visualization
        List<Map<String, Object>> enhancedWards = wards.stream().map(w -> {
            Map<String, Object> map = new HashMap<>();
            String name = w.getName() != null ? w.getName() : "Ward";
            int total = w.getCapacity() != null ? w.getCapacity() : 0;
            int occupied = w.getOccupied() != null ? w.getOccupied() : 0;
            int available = w.getAvailable() != null ? w.getAvailable() : total;

            map.put("id", w.getId());
            map.put("name", name);
            map.put("totalBeds", total); // Keep key as 'totalBeds' for frontend compatibility
            map.put("occupied", occupied);
            map.put("available", available);
            
            // Mock beds
            List<Map<String, String>> beds = new java.util.ArrayList<>();
            char prefix = name.length() > 0 ? name.charAt(name.length()-1) : 'U';
            for (int i = 1; i <= total; i++) {
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
