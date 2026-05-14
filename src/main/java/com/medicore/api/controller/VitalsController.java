package com.medicore.api.controller;

import com.medicore.api.model.Patient;
import com.medicore.api.model.Vitals;
import com.medicore.api.repository.PatientRepository;
import com.medicore.api.repository.VitalsRepository;
import com.medicore.api.util.ActivityLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class VitalsController {

    @Autowired
    private VitalsRepository vitalsRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ActivityLogger activityLogger;

    @GetMapping("/vitals")
    public ResponseEntity<?> getVitals(@RequestParam(required = false) Long patientId) {
        List<Vitals> vitals;
        if (patientId != null) {
            vitals = vitalsRepository.findByPatientIdOrderByMeasuredAtDesc(patientId);
        } else {
            vitals = vitalsRepository.findAll();
        }

        List<Map<String, Object>> result = vitals.stream().map(v -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", v.getId());
            m.put("patientId", v.getPatient() != null ? v.getPatient().getId() : null);
            m.put("patientName", v.getPatient() != null ? v.getPatient().getName() : "Unknown");
            m.put("bp", v.getBp());
            m.put("hr", v.getHr());
            m.put("temp", v.getTemp());
            m.put("spo2", v.getSpo2());
            m.put("rr", v.getRr());
            m.put("weight", v.getWeight());
            m.put("timestamp", v.getMeasuredAt() != null ? v.getMeasuredAt().toString() : "");
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("status", "success", "data", result));
    }

    @PostMapping("/vitals")
    public ResponseEntity<?> saveVitals(@RequestBody Map<String, Object> body) {
        try {
            if (!body.containsKey("patientId") || body.get("patientId") == null) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Patient ID is required"));
            }

            Vitals vitals = new Vitals();
            Long patientId = Long.parseLong(body.get("patientId").toString());
            Patient patient = patientRepository.findById(patientId).orElse(null);
            if (patient == null) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Patient not found"));
            }
            vitals.setPatient(patient);
            
            vitals.setBp(body.getOrDefault("bp", "N/A").toString());
            
            // Safe parsing helper
            vitals.setHr(parseSafeInt(body.get("hr"), 0));
            vitals.setTemp(parseSafeDouble(body.get("temp"), 0.0));
            vitals.setSpo2(parseSafeInt(body.get("spo2"), 0));
            vitals.setRr(parseSafeInt(body.get("rr"), 0));
            vitals.setWeight(parseSafeDouble(body.get("weight"), 0.0));
            
            vitals.setMeasuredAt(LocalDateTime.now());
            vitalsRepository.save(vitals);
            
            activityLogger.log(
                "ActivityIcon",
                "Clinical vitals recorded: BP " + vitals.getBp() + ", HR " + vitals.getHr() + " for " + patient.getName(),
                patient.getName()
            );
            return ResponseEntity.ok(Map.of("status", "success", "message", "Vitals recorded successfully", "data", vitals));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", "Processing error: " + e.getMessage()));
        }
    }

    private int parseSafeInt(Object value, int defaultVal) {
        if (value == null || value.toString().isEmpty()) return defaultVal;
        try {
            return (int) Double.parseDouble(value.toString()); // Handle case where it might be sent as 70.0
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private double parseSafeDouble(Object value, double defaultVal) {
        if (value == null || value.toString().isEmpty()) return defaultVal;
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return defaultVal;
        }
    }

}
