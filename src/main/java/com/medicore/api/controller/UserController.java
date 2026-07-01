package com.medicore.api.controller;

import com.medicore.api.model.User;
import com.medicore.api.model.Doctor;
import com.medicore.api.model.Nurse;
import com.medicore.api.repository.*;
import com.medicore.api.util.WhatsAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private NurseRepository nurseRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private LabTechnicianRepository labTechnicianRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestParam(required = false) String role) {
        List<User> users;
        if (role != null && !role.isEmpty()) {
            users = userRepository.findAll().stream()
                    .filter(u -> u.getRole().name().equalsIgnoreCase(role))
                    .toList();
        } else {
            users = userRepository.findAll();
        }
        List<Map<String, Object>> safeUsers = users.stream().map(u -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", u.getId());
            m.put("name", u.getName());
            m.put("username", u.getUsername());
            m.put("email", u.getEmail());
            m.put("phone", u.getPhone());
            m.put("role", u.getRole());
            m.put("status", u.getStatus());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).toList();
        return ResponseEntity.ok(Map.of("status", "success", "data", safeUsers));
    }

    @PostMapping("/users")
    @Transactional
    public ResponseEntity<?> saveUser(@RequestBody User user) {
        // If updating
        if (user.getId() != null) {
            Optional<User> existing = userRepository.findById(user.getId());
            if (existing.isPresent()) {
                User u = existing.get();
                u.setName(user.getName());
                u.setUsername(user.getUsername());
                if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                    u.setPassword(passwordEncoder.encode(user.getPassword()));
                }
                u.setEmail(user.getEmail());
                u.setRole(user.getRole());
                u.setStatus(user.getStatus());
                userRepository.save(u);
                return ResponseEntity.ok(Map.of("status", "success", "message", "User updated successfully"));
            }
        }
        
        // If creating
        if (user.getPhone() == null || user.getPhone().isEmpty() || user.getPassword() == null || user.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Phone number and password are required to create a new user and send login credentials."
            ));
        }

        String plainTextPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        
        String message = "MediCore Clinical Access\n\n" +
                "Welcome " + user.getName() + "!\n\n" +
                "Your system credentials have been created:\n\n" +
                "Username: " + user.getUsername() + "\n" +
                "Temporary Password: " + plainTextPassword + "\n" +
                "Role: " + user.getRole().name() + "\n\n" +
                "IMPORTANT:\n" +
                "1. Log in to the system immediately\n" +
                "2. Change your password to a secure one\n" +
                "3. Do not share these credentials\n\n" +
                "System: http://localhost:3000\n\n" +
                "Questions? Contact your administrator.";
        
        boolean sent = whatsAppService.sendMessage(user.getPhone(), message);
        if (!sent) {
            throw new RuntimeException("Failed to deliver WhatsApp credentials to the new user.");
        }
        
        return ResponseEntity.ok(Map.of("status", "success", "message", "User created successfully"));
    }

    @DeleteMapping("/users")
    public ResponseEntity<?> deleteUser(@RequestParam Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "User not found"));
        }

        User user = userOpt.get();

        // Check if user is a DOCTOR
        if (user.getRole() == User.Role.DOCTOR) {
            Optional<Doctor> doctorOpt = doctorRepository.findByUser(user);
            if (doctorOpt.isPresent()) {
                Doctor doctor = doctorOpt.get();
                
                // Check if doctor has pending or confirmed appointments
                List<Long> appointmentIds = appointmentRepository.findByDoctorId(doctor.getId()).stream()
                    .filter(a -> !a.getStatus().equalsIgnoreCase("completed") && !a.getStatus().equalsIgnoreCase("cancelled"))
                    .map(a -> a.getId())
                    .toList();
                
                if (!appointmentIds.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "status", "error", 
                        "message", "Cannot delete doctor with active appointments. Reassign or cancel appointments first.",
                        "activeAppointments", appointmentIds.size()
                    ));
                }
                
                // Delete doctor record
                doctorRepository.deleteById(doctor.getId());
            }
        }

        // Check if user is a NURSE
        if (user.getRole() == User.Role.NURSE) {
            Optional<Nurse> nurseOpt = nurseRepository.findByUser(user);
            if (nurseOpt.isPresent()) {
                Nurse nurse = nurseOpt.get();
                nurseRepository.deleteById(nurse.getId());
            }
        }

        // Check if user is a PATIENT
        if (user.getRole() == User.Role.PATIENT) {
            Optional<com.medicore.api.model.Patient> patientOpt = patientRepository.findByUser(user);
            if (patientOpt.isPresent()) {
                com.medicore.api.model.Patient patient = patientOpt.get();
                patientRepository.deleteById(patient.getId());
            }
        }

        // Check if user is a LABTECH
        if (user.getRole() == User.Role.LABTECH) {
            Optional<com.medicore.api.model.LabTechnician> labtechOpt = labTechnicianRepository.findByUser(user);
            if (labtechOpt.isPresent()) {
                com.medicore.api.model.LabTechnician labtech = labtechOpt.get();
                labTechnicianRepository.deleteById(labtech.getId());
            }
        }

        // Finally delete the user
        userRepository.deleteById(id);
        
        return ResponseEntity.ok(Map.of(
            "status", "success", 
            "message", user.getRole().name() + " account deleted successfully"
        ));
    }
}
