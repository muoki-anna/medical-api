package com.medicore.api.controller;

import com.medicore.api.model.Billing;
import com.medicore.api.model.ReportArtifact;
import com.medicore.api.repository.PatientRepository;
import com.medicore.api.repository.AppointmentRepository;
import com.medicore.api.repository.LabTestRepository;
import com.medicore.api.repository.ReportArtifactRepository;
import com.medicore.api.repository.BillingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private LabTestRepository labTestRepository;

    @Autowired
    private ReportArtifactRepository reportArtifactRepository;

    @Autowired
    private BillingRepository billingRepository;

    @GetMapping("/reports")
    public ResponseEntity<?> getReportStats() {
        Double turnover = billingRepository.sumPaidBills();
        if (turnover == null) turnover = 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPatients", patientRepository.count());
        stats.put("totalAppointments", appointmentRepository.count());
        stats.put("grossTurnover", String.format("KES %.1fK", turnover / 1000));
        stats.put("resolvedCases", appointmentRepository.countByStatus("completed"));
        stats.put("pendingDiagnostics", labTestRepository.countByStatus("pending")); 
        
        List<ReportArtifact> artifacts = reportArtifactRepository.findAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", stats);
        response.put("artifacts", artifacts);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reports")
    public ResponseEntity<?> generateArtifact(@RequestBody Map<String, String> body) {
        String domain = body.getOrDefault("domain", "General");
        String name = domain + " Insight " + LocalDate.now();
        
        ReportArtifact artifact = new ReportArtifact();
        artifact.setName(name);
        artifact.setDomain(domain);
        artifact.setTimestamp(LocalDate.now());
        artifact.setOriginator("Admin");
        
        reportArtifactRepository.save(artifact);
        
        return ResponseEntity.ok(Map.of("status", "success", "data", artifact));
    }
}
