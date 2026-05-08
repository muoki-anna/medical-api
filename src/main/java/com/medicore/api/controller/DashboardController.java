package com.medicore.api.controller;

import com.medicore.api.model.Patient;
import com.medicore.api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private LabTestRepository labTestRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<?> handleDashboard(@RequestParam(required = false) String action) {
        if ("widgets".equalsIgnoreCase(action)) {
            return getWidgets();
        } else if ("charts".equalsIgnoreCase(action)) {
            return getCharts();
        } else if ("recent_transactions".equalsIgnoreCase(action)) {
            return getRecentActivities();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", getDashboardSummary());
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> getWidgets() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalPatients", patientRepository.count());
        data.put("totalDoctors", doctorRepository.count());
        data.put("totalAppointments", appointmentRepository.count());
        data.put("pendingLabs", labTestRepository.countByStatus("pending"));

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> getCharts() {
        List<Patient> patients = patientRepository.findAll();
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        
        Map<Integer, Long> counts = patients.stream()
                .map(p -> p.getAdmissionDate() != null ? p.getAdmissionDate() : (p.getCreatedAt() != null ? p.getCreatedAt().toLocalDate() : null))
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.groupingBy(java.time.LocalDate::getMonthValue, java.util.stream.Collectors.counting()));

        List<Map<String, Object>> admissions = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> point = new HashMap<>();
            point.put("label", monthNames[i-1]);
            point.put("value", counts.getOrDefault(i, 0L));
            admissions.add(point);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("admissions", admissions);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> getRecentActivities() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", activityRepository.findTop10ByOrderByTimestampDesc());
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalPatients", patientRepository.count());
        summary.put("totalDoctors", doctorRepository.count());
        summary.put("totalAppointments", appointmentRepository.count());
        summary.put("pendingLabs", labTestRepository.countByStatus("pending"));
        return summary;
    }
}
