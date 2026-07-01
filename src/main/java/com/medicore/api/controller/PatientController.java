package com.medicore.api.controller;

import com.medicore.api.model.Patient;
import com.medicore.api.model.User;
import com.medicore.api.repository.PatientRepository;
import com.medicore.api.repository.UserRepository;
import com.medicore.api.repository.WardRepository;
import com.medicore.api.util.ActivityLogger;
import com.medicore.api.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ActivityLogger activityLogger;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/patients")
    public ResponseEntity<?> getPatients() {
        List<Patient> patients = patientRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", patients);
        return ResponseEntity.ok(response);
    }

    /** Autocomplete endpoint — returns flat safe maps, max `limit` results */
    @GetMapping("/patients/search")
    public ResponseEntity<?> searchPatients(
            @RequestParam String name,
            @RequestParam(defaultValue = "8") int limit) {
        List<Map<String, Object>> results = patientRepository
                .findByNameContainingIgnoreCase(name.trim())
                .stream()
                .limit(Math.max(1, Math.min(limit, 50)))
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("name", p.getName());
                    m.put("gender", p.getGender());
                    m.put("bloodType", p.getBloodType());
                    m.put("status", p.getStatus());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(Map.of("status", "success", "data", results));
    }


    @PostMapping("/patients")
    public ResponseEntity<?> savePatient(@RequestBody Map<String, String> body) {
        String idStr = body.get("id");
        Patient patient;
        User user;
        boolean isNew = idStr == null || idStr.isEmpty();

        if (!isNew) {
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
                user.setPassword(passwordEncoder.encode(body.get("password")));
            }
            user.setRole(User.Role.PATIENT);
            user.setEmail(body.get("email"));
            user.setPhone(body.get("contact"));
            user = userRepository.save(user);
            patient.setUser(user);
        }

        patient.setName(body.get("name"));
        patient.setGender(body.get("gender"));
        patient.setContact(body.get("contact"));
        patient.setEmail(body.get("email"));
        patient.setAddress(body.get("address"));
        patient.setStatus(body.getOrDefault("status", "outpatient"));
        
        // Auto-set admission date for tracking
        if (patient.getAdmissionDate() == null) {
            patient.setAdmissionDate(java.time.LocalDate.now());
        }

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

        // Handle doctor assignment
        String doctorId = body.get("doctorId");
        if (doctorId != null && !doctorId.isEmpty()) {
            doctorRepository.findById(Long.parseLong(doctorId)).ifPresent(patient::setAssignedDoctor);
        } else if (body.containsKey("doctorId")) {
            patient.setAssignedDoctor(null); // Explicit removal
        }

        patientRepository.save(patient);

        // Log Activity
        activityLogger.log(
            isNew ? "PlusIcon" : "EditIcon",
            isNew ? "New patient registration: " + patient.getName() : "Patient record updated: " + patient.getName(),
            patient.getName()
        );

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
