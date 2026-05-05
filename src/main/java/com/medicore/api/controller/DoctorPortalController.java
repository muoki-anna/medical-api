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

    @GetMapping("/patients")
    public ResponseEntity<?> getPatients(@RequestParam Long doctorId) {
        List<Patient> patients = patientRepository.findByAssignedDoctorId(doctorId);
        return ResponseEntity.ok(Map.of("status", "success", "data", patients));
    }

    @GetMapping("/notes")
    public ResponseEntity<?> getNotes(@RequestParam Long patientId) {
        return ResponseEntity.ok(Map.of("status", "success", "data", doctorNoteRepository.findByPatientId(patientId)));
    }

    @PostMapping("/notes")
    public ResponseEntity<?> saveNote(@RequestBody Map<String, Object> body) {
        DoctorNote note = new DoctorNote();
        note.setPatient(patientRepository.findById(Long.valueOf(body.get("patientId").toString())).orElseThrow());
        note.setDoctor(doctorRepository.findById(Long.valueOf(body.get("doctorId").toString())).orElseThrow());
        note.setNoteDate(LocalDate.now());
        note.setContent(body.get("content").toString());
        note.setIsPrivate(Boolean.valueOf(body.getOrDefault("isPrivate", false).toString()));
        doctorNoteRepository.save(note);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Note saved"));
    }

    @PostMapping("/prescriptions")
    public ResponseEntity<?> savePrescription(@RequestBody Prescription prescription) {
        prescriptionRepository.save(prescription);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Prescription issued"));
    }

    @PostMapping("/lab-requests")
    public ResponseEntity<?> saveLabRequest(@RequestBody LabTest labTest) {
        labTest.setDateRequested(LocalDate.now());
        labTest.setStatus("pending");
        labTestRepository.save(labTest);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Lab request initiated"));
    }
}
