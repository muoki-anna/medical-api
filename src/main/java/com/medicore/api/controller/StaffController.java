package com.medicore.api.controller;

import com.medicore.api.model.*;
import com.medicore.api.repository.*;
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
public class StaffController {

    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private NurseRepository nurseRepository;
    @Autowired
    private LabTechnicianRepository labTechnicianRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WardRepository wardRepository;

    // --- DOCTORS ---

    @GetMapping("/doctors")
    public ResponseEntity<?> getDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Map<String, Object>> result = doctors.stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId());
            m.put("name", d.getName());
            m.put("specialization", d.getSpecialization());
            m.put("email", d.getEmail());
            m.put("phone", d.getPhone());
            m.put("ward", d.getWard()); 
            m.put("status", d.getStatus());
            m.put("username", d.getUser() != null ? d.getUser().getUsername() : "");
            return m;
        }).toList();
        return ResponseEntity.ok(Map.of("status", "success", "data", result));
    }

    @PostMapping("/doctors")
    public ResponseEntity<?> saveDoctor(@RequestBody Map<String, String> body) {
        String idStr = body.get("id");
        Doctor doctor;
        User user;

        if (idStr != null && !idStr.isEmpty()) {
            doctor = doctorRepository.findById(Long.parseLong(idStr)).orElse(new Doctor());
            user = doctor.getUser();
        } else {
            doctor = new Doctor();
            user = new User();
        }

        user.setName(body.get("name"));
        user.setUsername(body.get("username"));
        if (body.get("password") != null && !body.get("password").isEmpty()) {
            user.setPassword(body.get("password"));
        }
        user.setEmail(body.get("email"));
        user.setRole(User.Role.DOCTOR);
        user = userRepository.save(user);

        doctor.setUser(user);
        doctor.setName(body.get("name"));
        doctor.setSpecialization(body.get("specialization"));
        doctor.setEmail(body.get("email"));
        doctor.setPhone(body.get("phone"));
        
        String wardId = body.get("wardId");
        if (wardId != null && !wardId.isEmpty()) {
            doctor.setWard(wardRepository.findById(Long.parseLong(wardId)).orElse(null));
        } else if (body.containsKey("wardId")) {
            doctor.setWard(null);
        }

        doctorRepository.save(doctor);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Doctor saved successfully"));
    }

    @DeleteMapping("/doctors")
    public ResponseEntity<?> deleteDoctor(@RequestParam Long id) {
        doctorRepository.findById(id).ifPresent(d -> {
            User u = d.getUser();
            doctorRepository.delete(d);
            if (u != null) userRepository.delete(u);
        });
        return ResponseEntity.ok(Map.of("status", "success", "message", "Doctor deleted"));
    }

    // --- NURSES ---

    @GetMapping("/nurses")
    public ResponseEntity<?> getNurses() {
        List<Nurse> nurses = nurseRepository.findAll();
        List<Map<String, Object>> result = nurses.stream().map(n -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", n.getId());
            m.put("name", n.getName());
            m.put("email", n.getEmail());
            m.put("phone", n.getPhone());
            m.put("ward", n.getWard());
            m.put("shift", n.getShift());
            m.put("status", n.getStatus());
            m.put("username", n.getUser() != null ? n.getUser().getUsername() : "");
            return m;
        }).toList();
        return ResponseEntity.ok(Map.of("status", "success", "data", result));
    }

    @PostMapping("/nurses")
    public ResponseEntity<?> saveNurse(@RequestBody Map<String, String> body) {
        String idStr = body.get("id");
        Nurse nurse;
        User user;

        if (idStr != null && !idStr.isEmpty()) {
            nurse = nurseRepository.findById(Long.parseLong(idStr)).orElse(new Nurse());
            user = nurse.getUser();
        } else {
            nurse = new Nurse();
            user = new User();
        }

        user.setName(body.get("name"));
        user.setUsername(body.get("username"));
        if (body.get("password") != null && !body.get("password").isEmpty()) {
            user.setPassword(body.get("password"));
        }
        user.setEmail(body.get("email"));
        user.setRole(User.Role.NURSE);
        user = userRepository.save(user);

        nurse.setUser(user);
        nurse.setName(body.get("name"));
        nurse.setEmail(body.get("email"));
        nurse.setPhone(body.get("phone"));
        String shiftStr = body.get("shift");
        if (shiftStr != null && !shiftStr.isEmpty()) {
            try {
                nurse.setShift(Nurse.Shift.valueOf(shiftStr));
            } catch (IllegalArgumentException e) {
                nurse.setShift(Nurse.Shift.Morning); // Default fallback
            }
        }
        
        String wardId = body.get("wardId");
        if (wardId != null && !wardId.isEmpty()) {
            nurse.setWard(wardRepository.findById(Long.parseLong(wardId)).orElse(null));
        } else if (body.containsKey("wardId")) {
            nurse.setWard(null);
        }

        nurseRepository.save(nurse);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Nurse saved successfully"));
    }

    @DeleteMapping("/nurses")
    public ResponseEntity<?> deleteNurse(@RequestParam Long id) {
        nurseRepository.findById(id).ifPresent(n -> {
            User u = n.getUser();
            nurseRepository.delete(n);
            if (u != null) userRepository.delete(u);
        });
        return ResponseEntity.ok(Map.of("status", "success", "message", "Nurse deleted"));
    }

    // --- LAB TECHS ---

    @GetMapping("/labtechs")
    public ResponseEntity<?> getLabTechs() {
        List<LabTechnician> labtechs = labTechnicianRepository.findAll();
        List<Map<String, Object>> result = labtechs.stream().map(l -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", l.getId());
            m.put("name", l.getName());
            m.put("email", l.getEmail());
            m.put("phone", l.getPhone());
            m.put("status", l.getStatus());
            m.put("username", l.getUser() != null ? l.getUser().getUsername() : "");
            return m;
        }).toList();
        return ResponseEntity.ok(Map.of("status", "success", "data", result));
    }

    @PostMapping("/labtechs")
    public ResponseEntity<?> saveLabTech(@RequestBody Map<String, String> body) {
        String idStr = body.get("id");
        LabTechnician labTech;
        User user;

        if (idStr != null && !idStr.isEmpty()) {
            labTech = labTechnicianRepository.findById(Long.parseLong(idStr)).orElse(new LabTechnician());
            user = labTech.getUser();
        } else {
            labTech = new LabTechnician();
            user = new User();
        }

        user.setName(body.get("name"));
        user.setUsername(body.get("username"));
        if (body.get("password") != null && !body.get("password").isEmpty()) {
            user.setPassword(body.get("password"));
        }
        user.setEmail(body.get("email"));
        user.setRole(User.Role.LABTECH);
        user = userRepository.save(user);

        labTech.setUser(user);
        labTech.setName(body.get("name"));
        labTech.setEmail(body.get("email"));
        labTech.setPhone(body.get("phone"));

        labTechnicianRepository.save(labTech);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Lab technician saved successfully"));
    }

    @DeleteMapping("/labtechs")
    public ResponseEntity<?> deleteLabTech(@RequestParam Long id) {
        labTechnicianRepository.findById(id).ifPresent(l -> {
            User u = l.getUser();
            labTechnicianRepository.delete(l);
            if (u != null) userRepository.delete(u);
        });
        return ResponseEntity.ok(Map.of("status", "success", "message", "Lab technician deleted"));
    }
}
