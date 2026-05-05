package com.medicore.api.controller;

import com.medicore.api.model.Patient;
import com.medicore.api.model.User;
import com.medicore.api.repository.PatientRepository;
import com.medicore.api.repository.UserRepository;
import com.medicore.api.model.Ward;
import com.medicore.api.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WardRepository wardRepository;

    @GetMapping("/patients")
    public ResponseEntity<?> getPatients() {
        List<Patient> patients = patientRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", patients);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/patients")
    public ResponseEntity<?> savePatient(@RequestBody Map<String, String> body) {
        String idStr = body.get("id");
        Patient patient;
        User user;

        if (idStr != null && !idStr.isEmpty()) {
            patient = patientRepository.findById(Long.parseLong(idStr)).orElse(new Patient());
            user = patient.getUser();
        } else {
            patient = new Patient();
            user = new User();
        }

        // Create/Update user if needed (for patient portal)
        // If username is provided, we manage the user account
        String username = body.get("username");
        if (username != null && !username.isEmpty()) {
            user.setName(body.get("name"));
            user.setUsername(username);
            if (body.get("password") != null && !body.get("password").isEmpty()) {
                user.setPassword(body.get("password"));
            }
            user.setRole(User.Role.PATIENT);
            user.setEmail(body.get("email"));
            user = userRepository.save(user);
            patient.setUser(user);
        }

        patient.setName(body.get("name"));
        patient.setGender(body.get("gender"));
        patient.setContact(body.get("contact"));
        patient.setEmail(body.get("email"));
        patient.setStatus(body.getOrDefault("status", "outpatient"));
        
        // Handle optional fields if they exist in frontend body
        if (body.containsKey("blood_type")) patient.setBloodType(body.get("blood_type"));
        if (body.containsKey("diagnosis")) patient.setDiagnosis(body.get("diagnosis"));
        if (body.containsKey("patient_code")) patient.setPatientCode(body.get("patient_code"));

        // Handle ward allocation
        String wardId = body.get("wardId");
        if (wardId != null && !wardId.isEmpty()) {
            wardRepository.findById(Long.parseLong(wardId)).ifPresent(patient::setWard);
        } else if (body.containsKey("wardId")) {
            patient.setWard(null); // Explicit removal
        }

        patientRepository.save(patient);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Patient saved successfully"));
    }

    @DeleteMapping("/patients")
    public ResponseEntity<?> deletePatient(@RequestParam Long id) {
        patientRepository.findById(id).ifPresent(p -> {
            User u = p.getUser();
            patientRepository.delete(p);
            if (u != null) userRepository.delete(u);
        });
        return ResponseEntity.ok(Map.of("status", "success", "message", "Patient deleted"));
    }
}
