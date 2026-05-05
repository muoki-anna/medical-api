package com.medicore.api.controller;

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
        List<Map<String, Object>> admissions = new ArrayList<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        
        // For now, if we have no monthly data, we show 0s to be 'true' to the database state
        // In a real app, this would be a GROUP BY query on createdAt
        for (String month : months) {
            Map<String, Object> point = new HashMap<>();
            point.put("label", month);
            point.put("value", 0); 
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
        summary.put("patientCount", patientRepository.count());
        summary.put("doctorCount", doctorRepository.count());
        summary.put("appointmentCount", appointmentRepository.count());
        return summary;
    }
}
