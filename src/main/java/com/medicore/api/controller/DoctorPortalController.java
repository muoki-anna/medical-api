package com.medicore.api.controller;

import com.medicore.api.model.*;
import com.medicore.api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
@CrossOrigin(origins = "*")
public class DoctorPortalController {

    @Autowired private DoctorNoteRepository doctorNoteRepository;
    @Autowired private PrescriptionRepository prescriptionRepository;
    @Autowired private LabTestRepository labTestRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private AppointmentRepository appointmentRepository;

    private Long resolveDoctorId(Long id) {
        // Try to find the doctor by its associated User ID first, 
        // as the frontend typically sends the logged-in user's ID.
        return doctorRepository.findByUserId(id)
                .map(Doctor::getId)
                .orElse(id); // Fallback to treating it as a Doctor ID if no User mapping found
    }

    @GetMapping("/patients")
    public ResponseEntity<?> getPatients(@RequestParam Long doctorId) {
        Long resolvedId = resolveDoctorId(doctorId);
        List<Patient> patients = patientRepository.findByAssignedDoctorId(resolvedId);
        return ResponseEntity.ok(Map.of("status", "success", "data", patients));
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointments(@RequestParam Long doctorId) {
        Long resolvedId = resolveDoctorId(doctorId);
        List<Appointment> appointments = appointmentRepository.findByDoctorId(resolvedId);
        return ResponseEntity.ok(Map.of("status", "success", "data", appointments));
    }

    @GetMapping("/notes")
    public ResponseEntity<?> getNotes(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId) {
        if (patientId != null) {
            return ResponseEntity.ok(Map.of("status", "success", "data", doctorNoteRepository.findByPatientId(patientId)));
        } else if (doctorId != null) {
            Long resolvedId = resolveDoctorId(doctorId);
            return ResponseEntity.ok(Map.of("status", "success", "data", doctorNoteRepository.findByDoctorId(resolvedId)));
        }
        return ResponseEntity.ok(Map.of("status", "success", "data", List.of()));
    }

    @PostMapping("/notes")
    public ResponseEntity<?> saveNote(@RequestBody Map<String, Object> body) {
        try {
            DoctorNote note = new DoctorNote();
            
            // Extract patient ID - handle both flat "patientId" and nested "patient: { id: ... }"
            final Long patientId;
            if (body.get("patientId") != null) {
                patientId = Long.valueOf(body.get("patientId").toString());
            } else if (body.get("patient") instanceof Map) {
                Map<?, ?> patientMap = (Map<?, ?>) body.get("patient");
                patientId = patientMap.get("id") != null ? Long.valueOf(patientMap.get("id").toString()) : null;
            } else {
                patientId = null;
            }
            
            if (patientId == null) throw new IllegalArgumentException("Patient ID is missing");
            note.setPatient(patientRepository.findById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId)));
            
            // Extract doctor ID - handle both flat "doctorId" and nested "doctor: { id: ... }"
            final Long rawDocId;
            if (body.get("doctorId") != null) {
                rawDocId = Long.valueOf(body.get("doctorId").toString());
            } else if (body.get("doctor") instanceof Map) {
                Map<?, ?> doctorMap = (Map<?, ?>) body.get("doctor");
                rawDocId = doctorMap.get("id") != null ? Long.valueOf(doctorMap.get("id").toString()) : null;
            } else {
                rawDocId = null;
            }
            
            if (rawDocId == null) throw new IllegalArgumentException("Doctor ID is missing");
            Long resolvedDocId = resolveDoctorId(rawDocId);
            note.setDoctor(doctorRepository.findById(resolvedDocId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + resolvedDocId)));
            
            note.setNoteDate(LocalDate.now());
            note.setContent(body.getOrDefault("content", "").toString());
            note.setIsPrivate(Boolean.valueOf(body.getOrDefault("isPrivate", false).toString()));
            
            doctorNoteRepository.save(note);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Note saved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/prescriptions")
    public ResponseEntity<?> getPrescriptions(@RequestParam Long doctorId) {
        Long resolvedId = resolveDoctorId(doctorId);
        List<Prescription> prescriptions = prescriptionRepository.findByDoctorId(resolvedId);
        return ResponseEntity.ok(Map.of("status", "success", "data", prescriptions));
    }

    @GetMapping("/lab-requests")
    public ResponseEntity<?> getLabRequests(@RequestParam Long doctorId) {
        Long resolvedId = resolveDoctorId(doctorId);
        List<LabTest> labTests = labTestRepository.findByDoctorId(resolvedId);
        return ResponseEntity.ok(Map.of("status", "success", "data", labTests));
    }

    @PostMapping("/prescriptions")
    public ResponseEntity<?> savePrescription(@RequestBody Prescription prescription) {
        if (prescription.getDoctor() != null && prescription.getDoctor().getId() != null) {
            Long resolvedId = resolveDoctorId(prescription.getDoctor().getId());
            prescription.setDoctor(doctorRepository.findById(resolvedId).orElse(prescription.getDoctor()));
        }
        prescriptionRepository.save(prescription);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Prescription issued"));
    }

    @PostMapping("/lab-requests")
    public ResponseEntity<?> saveLabRequest(@RequestBody LabTest labTest) {
        if (labTest.getDoctor() != null && labTest.getDoctor().getId() != null) {
            Long resolvedId = resolveDoctorId(labTest.getDoctor().getId());
            labTest.setDoctor(doctorRepository.findById(resolvedId).orElse(labTest.getDoctor()));
        }
        labTest.setDateRequested(LocalDate.now());
        labTest.setStatus("pending");
        labTestRepository.save(labTest);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Lab request initiated"));
    }
}
