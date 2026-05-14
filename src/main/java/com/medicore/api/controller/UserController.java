package com.medicore.api.controller;

import com.medicore.api.model.User;
import com.medicore.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("status", "success", "message", "User created successfully"));
    }

    @DeleteMapping("/users")
    public ResponseEntity<?> deleteUser(@RequestParam Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "success", "message", "User deleted"));
    }
}
