package com.medicore.api.controller;

import com.medicore.api.model.Appointment;
import com.medicore.api.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointments() {
        List<Appointment> appointments = appointmentRepository.findAll();
        List<Map<String, Object>> result = appointments.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("patient", a.getPatient() != null ? a.getPatient().getName() : "Unknown");
            m.put("doctor", a.getDoctor() != null ? a.getDoctor().getName() : "Unassigned");
            m.put("date", a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : "N/A");
            m.put("time", a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : "N/A");
            m.put("reason", a.getReason());
            m.put("department", a.getDepartment());
            m.put("status", a.getStatus());
            return m;
        }).toList();
        return ResponseEntity.ok(Map.of("status", "success", "data", result));
    }

    @PostMapping("/appointments")
    public ResponseEntity<?> createAppointment(@RequestBody Appointment appointment) {
        Appointment saved = appointmentRepository.save(appointment);
        return ResponseEntity.ok(Map.of("status", "success", "data", saved));
    }

    @DeleteMapping("/appointments")
    public ResponseEntity<?> deleteAppointment(@RequestParam Long id) {
        appointmentRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Appointment deleted"));
    }
}
