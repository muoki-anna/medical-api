package com.medicore.api.controller;

import com.medicore.api.model.LabTest;
import com.medicore.api.repository.LabTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/labtech")
@CrossOrigin(origins = "*")
public class LabPortalController {

    @Autowired
    private LabTestRepository labTestRepository;

    @GetMapping("/queue")
    public ResponseEntity<?> getQueue(@RequestParam(required = false) Long patientId) {
        List<LabTest> requests;
        if (patientId != null) {
            requests = labTestRepository.findByPatientId(patientId);
        } else {
            requests = labTestRepository.findAll();
        }
        
        List<Map<String, Object>> result = requests.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("requestCode", r.getTestCode());
            m.put("patient", r.getPatient() != null ? r.getPatient().getName() : "Unknown");
            m.put("testType", r.getTestType());
            m.put("urgency", r.getUrgency());
            m.put("dateRequested", r.getDateRequested() != null ? r.getDateRequested().toString() : "");
            m.put("status", r.getStatus());
            m.put("result", r.getResult());
            m.put("doctor", r.getDoctor() != null ? r.getDoctor().getName() : "Unknown");
            return m;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of("status", "success", "data", result));
    }

    @PostMapping("/results")
    public ResponseEntity<?> updateResults(@RequestBody Map<String, String> body) {
        Long id = Long.parseLong(body.get("id"));
        String result = body.get("result");
        String status = body.getOrDefault("status", "completed");
        
        LabTest request = labTestRepository.findById(id).orElseThrow();
        request.setResult(result);
        request.setStatus(status);
        labTestRepository.save(request);
        
        return ResponseEntity.ok(Map.of("status", "success", "message", "Results updated"));
    }
}
