package com.medicore.api.controller;

import com.medicore.api.model.Appointment;
import com.medicore.api.repository.*;
import com.medicore.api.util.ActivityLogger;
import com.medicore.api.util.WhatsAppService;
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

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private ActivityLogger activityLogger;

    private boolean notifyPatientAboutAppointment(Appointment appointment, String action) {
        if (appointment == null || appointment.getPatient() == null) {
            return false;
        }

        String phone = null;
        if (appointment.getPatient().getContact() != null && !appointment.getPatient().getContact().isBlank()) {
            phone = appointment.getPatient().getContact();
        } else if (appointment.getPatient().getUser() != null && appointment.getPatient().getUser().getPhone() != null && !appointment.getPatient().getUser().getPhone().isBlank()) {
            phone = appointment.getPatient().getUser().getPhone();
        }

        if (phone == null || phone.isBlank()) {
            return false;
        }

        String doctorPart = appointment.getDoctor() != null ? "Doctor: " + appointment.getDoctor().getName() + ". " : "";
        String date = appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "TBD";
        String time = appointment.getAppointmentTime() != null ? appointment.getAppointmentTime().toString() : "TBD";
        String department = appointment.getDepartment() != null && !appointment.getDepartment().isBlank() ? appointment.getDepartment() : "General";

        String message;
        if (appointment.getDoctor() == null) {
            message = "Your appointment request has been received. A doctor will be assigned when available. " +
                    "Date: " + date + ". Time: " + time + ". Department: " + department + ".";
        } else {
            message = "Your appointment has been " + action + ". " +
                    doctorPart +
                    "Date: " + date + ". Time: " + time + ". Department: " + department + ".";
        }

        return whatsAppService.sendMessage(phone, message);
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointments(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId) {
        
        List<Appointment> appointments;
        if (patientId != null) {
            appointments = appointmentRepository.findByPatientId(patientId);
        } else if (doctorId != null) {
            appointments = appointmentRepository.findByDoctorId(doctorId);
        } else {
            appointments = appointmentRepository.findAll();
        }

        List<Map<String, Object>> result = appointments.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("patient", a.getPatient() != null ? a.getPatient().getName() : "Unknown");
            m.put("patientId", a.getPatient() != null ? a.getPatient().getId() : null);
            m.put("doctor", a.getDoctor() != null ? a.getDoctor().getName() : "Unassigned");
            m.put("doctorId", a.getDoctor() != null ? a.getDoctor().getId() : null);
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
    public ResponseEntity<?> createAppointment(@RequestBody Map<String, Object> body) {
        System.out.println("[DEBUG] Received appointment body: " + body);
        try {
            Appointment appointment = new Appointment();
            appointment.setReason((String) body.get("reason"));
            appointment.setDepartment((String) body.get("department"));
            appointment.setStatus((String) body.getOrDefault("status", "pending"));
            
            // Handle different naming conventions (date/time vs appointmentDate/appointmentTime)
            String dateStr = body.containsKey("date") ? (String) body.get("date") : (String) body.get("appointmentDate");
            String timeStr = body.containsKey("time") ? (String) body.get("time") : (String) body.get("appointmentTime");

            if (dateStr == null || dateStr.trim().isEmpty() || timeStr == null || timeStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Protocol Error: Appointment date and time are mandatory clinical parameters."));
            }

            appointment.setAppointmentDate(java.time.LocalDate.parse(dateStr));
            appointment.setAppointmentTime(java.time.LocalTime.parse(timeStr));

            // Generate unique clinical identifier
            appointment.setAppointmentCode("APT-" + System.currentTimeMillis());

            // Handle Patient (Nested or Flat ID)
            Object patientObj = body.get("patient");
            String patientIdStr = null;
            if (patientObj instanceof Map) {
                patientIdStr = String.valueOf(((Map<?, ?>) patientObj).get("id"));
            } else if (body.get("patientId") != null) {
                patientIdStr = body.get("patientId").toString();
            } else if (body.get("patient_id") != null) {
                patientIdStr = body.get("patient_id").toString();
            }

            if (patientIdStr != null && !patientIdStr.trim().isEmpty() && !"null".equals(patientIdStr)) {
                System.out.println("[DEBUG] Resolved Patient ID: " + patientIdStr);
                try {
                    patientRepository.findById(Long.valueOf(patientIdStr)).ifPresent(p -> {
                        System.out.println("[DEBUG] Found Patient: " + p.getName());
                        appointment.setPatient(p);
                    });
                } catch (NumberFormatException e) {
                    System.err.println("[DEBUG] Invalid Patient ID format: " + patientIdStr);
                }
            }

            // Handle Doctor (Nested or Flat ID)
            Object doctorObj = body.get("doctor");
            String doctorIdStr = null;
            if (doctorObj instanceof Map) {
                doctorIdStr = String.valueOf(((Map<?, ?>) doctorObj).get("id"));
            } else if (body.get("doctorId") != null) {
                doctorIdStr = body.get("doctorId").toString();
            } else if (body.get("doctor_id") != null) {
                doctorIdStr = body.get("doctor_id").toString();
            }

            if (doctorIdStr != null && !doctorIdStr.trim().isEmpty() && !"null".equals(doctorIdStr)) {
                System.out.println("[DEBUG] Resolved Doctor ID: " + doctorIdStr);
                try {
                    doctorRepository.findById(Long.valueOf(doctorIdStr)).ifPresent(d -> {
                        System.out.println("[DEBUG] Found Doctor: " + d.getName());
                        appointment.setDoctor(d);
                    });
                } catch (NumberFormatException e) {
                    System.err.println("[DEBUG] Invalid Doctor ID format: " + doctorIdStr);
                }
            }

            Appointment saved = appointmentRepository.save(appointment);
            
            activityLogger.log(
                "CalendarIcon",
                "New clinical encounter requested for: " + (appointment.getPatient() != null ? appointment.getPatient().getName() : "Unknown"),
                appointment.getPatient() != null ? appointment.getPatient().getName() : "Unknown"
            );

            notifyPatientAboutAppointment(saved, "created");

            return ResponseEntity.ok(Map.of("status", "success", "data", saved));
        } catch (java.time.format.DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Invalid temporal format: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", "Clinical processing failure: " + e.getMessage()));
        }
    }

    @PatchMapping("/appointments/{id}/status")
    public ResponseEntity<?> updateAppointmentStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return appointmentRepository.findById(id).<ResponseEntity<?>>map(appt -> {
            String newStatus = body.get("status");
            if (newStatus != null && !newStatus.isBlank()) {
                appt.setStatus(newStatus);
                appointmentRepository.save(appt);
                
                activityLogger.log(
                    "ActivityIcon",
                    "Appointment status transitioned to " + newStatus + " for " + (appt.getPatient() != null ? appt.getPatient().getName() : "Unknown"),
                    appt.getPatient() != null ? appt.getPatient().getName() : "Unknown"
                );
            }
            return ResponseEntity.ok(Map.of("status", "success", "message", "Appointment status updated to " + newStatus));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/appointments/{id}")
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return appointmentRepository.findById(id).<ResponseEntity<?>>map(appt -> {
            if (body.containsKey("status")) appt.setStatus((String) body.get("status"));
            if (body.containsKey("reason")) appt.setReason((String) body.get("reason"));
            if (body.containsKey("department")) appt.setDepartment((String) body.get("department"));

            if (body.containsKey("doctor")) {
                Object doctorObj = body.get("doctor");
                String doctorIdStr = null;
                if (doctorObj instanceof Map) {
                    doctorIdStr = String.valueOf(((Map<?, ?>) doctorObj).get("id"));
                } else if (body.get("doctorId") != null) {
                    doctorIdStr = body.get("doctorId").toString();
                }
                if (doctorIdStr != null && !doctorIdStr.trim().isEmpty() && !"null".equals(doctorIdStr)) {
                    try {
                        doctorRepository.findById(Long.valueOf(doctorIdStr)).ifPresent(appt::setDoctor);
                    } catch (NumberFormatException e) {
                        System.err.println("[DEBUG] Invalid doctor ID format: " + doctorIdStr);
                    }
                }
            }

            if (body.containsKey("date")) {
                try { appt.setAppointmentDate(java.time.LocalDate.parse((String) body.get("date"))); } catch (Exception ignored) {}
            }
            if (body.containsKey("time")) {
                try { appt.setAppointmentTime(java.time.LocalTime.parse((String) body.get("time"))); } catch (Exception ignored) {}
            }
            appointmentRepository.save(appt);
            notifyPatientAboutAppointment(appt, "updated");
            return ResponseEntity.ok(Map.of("status", "success", "message", "Appointment updated"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/appointments")
    public ResponseEntity<?> deleteAppointment(@RequestParam Long id) {
        appointmentRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Appointment deleted"));
    }
}
