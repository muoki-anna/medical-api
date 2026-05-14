package com.medicore.api.controller;

import com.medicore.api.model.User;
import com.medicore.api.repository.UserRepository;
import com.medicore.api.repository.DoctorRepository;
import com.medicore.api.repository.NurseRepository;
import com.medicore.api.util.ActivityLogger;
import com.medicore.api.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private NurseRepository nurseRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ActivityLogger activityLogger;

    // ── POST /api/auth  (login + body-based actions) ──────────────────────
    @PostMapping("/auth")
    public ResponseEntity<?> handleAuthPost(
            @RequestParam(required = false) String action,
            @RequestBody Map<String, String> body) {

        String effectiveAction = action != null ? action : body.getOrDefault("action", "");

        return switch (effectiveAction.toLowerCase()) {
            case "login"  -> login(body.get("username"), body.get("password"));
            case "logout" -> ResponseEntity.ok(Map.of("status", "success", "message", "Logged out"));
            default       -> ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Unknown action: " + effectiveAction));
        };
    }

    // ── GET /api/auth  (token verify) ─────────────────────────────────────
    @GetMapping("/auth")
    public ResponseEntity<?> handleAuthGet(
            @RequestParam(required = false) String action,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isTokenValid(token)) {
                return ResponseEntity.ok(Map.of("status", "success", "message", "Token is valid"));
            }
        }

        return ResponseEntity.status(401)
                .body(Map.of("status", "error", "message", "Session expired or invalid token"));
    }

    // ── Private helpers ────────────────────────────────────────────────────────
    private ResponseEntity<?> login(String username, String password) {
        if (username == null || password == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Username and password are required"));
        }

        Optional<User> userOpt = userRepository.findByUsername(username.trim());

        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            User user = userOpt.get();
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getName());

            activityLogger.log(
                "LockIcon",
                "User session started: " + user.getName() + " (" + user.getRole() + ")",
                user.getName()
            );

            Map<String, Object> userData = new HashMap<>();
            userData.put("id",       user.getId());
            userData.put("name",     user.getName());
            userData.put("username", user.getUsername());
            userData.put("role",     user.getRole().name().toLowerCase());
            userData.put("email",    user.getEmail() != null ? user.getEmail() : "");

            // Include Ward Info for clinical roles
            if (user.getRole() == User.Role.DOCTOR) {
                doctorRepository.findByUser(user).ifPresent(d -> {
                    if (d.getWard() != null) userData.put("ward", d.getWard());
                });
            } else if (user.getRole() == User.Role.NURSE) {
                nurseRepository.findByUser(user).ifPresent(n -> {
                    if (n.getWard() != null) userData.put("ward", n.getWard());
                });
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("user",  userData);

            return ResponseEntity.ok(Map.of("status", "success", "data", responseData));
        }

        return ResponseEntity.status(401)
                .body(Map.of("status", "error", "message", "Invalid username or password"));
    }
}
