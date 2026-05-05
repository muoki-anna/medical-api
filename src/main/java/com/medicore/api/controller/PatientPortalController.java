package com.medicore.api.controller;

import com.medicore.api.model.*;
import com.medicore.api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient")
@CrossOrigin(origins = "*")
public class PatientPortalController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private LabTestRepository labTestRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private VitalsRepository vitalsRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestParam Long patientId) {
        Map<String, Object> data = new HashMap<>();
        
        // Next Appointment
        Appointment nextAppt = appointmentRepository.findAll().stream()
                .filter(a -> a.getPatient() != null && a.getPatient().getId().equals(patientId))
                .findFirst().orElse(null);
        
        if (nextAppt != null) {
            Map<String, Object> appt = new HashMap<>();
            appt.put("date", nextAppt.getAppointmentDate());
            appt.put("time", nextAppt.getAppointmentTime());
            appt.put("doctor", nextAppt.getDoctor() != null ? nextAppt.getDoctor().getName() : "Unknown");
            appt.put("department", nextAppt.getDoctor() != null ? nextAppt.getDoctor().getSpecialization() : "General");
            appt.put("reason", nextAppt.getReason());
            data.put("nextAppointment", appt);
        }

        // Stats
        data.put("activePrescriptions", prescriptionRepository.findByPatientId(patientId).size());
        data.put("pendingLabs", labTestRepository.findByPatientId(patientId).stream().filter(l -> l.getStatus().equals("pending")).count());
        
        // Latest Vitals
        Vitals v = vitalsRepository.findAll().stream()
                .filter(vit -> vit.getPatient() != null && vit.getPatient().getId().equals(patientId))
                .findFirst().orElse(null);
        
        if (v != null) {
            data.put("bloodPressure", v.getBp());
            data.put("weight", v.getWeight());
            data.put("heartRate", v.getHr());
        }

        return ResponseEntity.ok(Map.of("status", "success", "data", data));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam Long patientId) {
        return patientRepository.findById(patientId)
                .map(p -> ResponseEntity.ok(Map.of("status", "success", "data", p)))
                .orElse(ResponseEntity.notFound().build());
    }
}
