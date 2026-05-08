package com.medicore.api.controller;

import com.medicore.api.model.Prescription;
import com.medicore.api.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PrescriptionController {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @GetMapping("/prescriptions")
    public ResponseEntity<?> getPrescriptions(@RequestParam(required = false) Long patientId) {
        List<Prescription> prescriptions;
        if (patientId != null) {
            prescriptions = prescriptionRepository.findByPatientId(patientId);
        } else {
            prescriptions = prescriptionRepository.findAll();
        }
        return ResponseEntity.ok(Map.of("status", "success", "data", prescriptions));
    }

    @PostMapping("/prescriptions/administer")
    public ResponseEntity<?> administer(@RequestBody Map<String, Object> body) {
        try {
            Long id = Long.parseLong(body.get("id").toString());
            Prescription p = prescriptionRepository.findById(id).orElse(null);
            if (p == null) return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Prescription not found"));
            
            p.setStatus("administered");
            // In a real app, we'd log who administered it and when.
            prescriptionRepository.save(p);
            
            return ResponseEntity.ok(Map.of("status", "success", "message", "Medication administered"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
