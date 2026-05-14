package com.medicore.api.controller;

import com.medicore.api.repository.*;
import com.medicore.api.model.*;
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
    private PatientRepository patientRepository;

    @Autowired
    private LabTechnicianRepository labTechnicianRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private com.medicore.api.util.WhatsAppService whatsappService;

    @Autowired
    private ActivityLogger activityLogger;

    // ── POST /api/auth  (login + body-based actions) ──────────────────────
    @PostMapping("/auth")
    public ResponseEntity<?> handleAuthPost(
            @RequestParam(required = false) String action,
            @RequestBody Map<String, String> body) {

        String effectiveAction = action != null ? action : body.getOrDefault("action", "");

        return switch (effectiveAction.toLowerCase()) {
            case "login"           -> login(body.get("username"), body.get("password"));
            case "logout"          -> ResponseEntity.ok(Map.of("status", "success", "message", "Logged out"));
            case "forgot_password" -> forgotPassword(body.get("username"));
            case "reset_password"  -> resetPassword(body.get("token"), body.get("password"));
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

    private ResponseEntity<?> forgotPassword(String username) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Username is required"));
        }

        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            // For security, don't reveal if user exists, but here the user wants "work" so we can be explicit
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "User not found"));
        }

        User user = userOpt.get();
        
        // Clean up old tokens
        tokenRepository.findByUser(user).ifPresent(t -> tokenRepository.delete(t));
        
        com.medicore.api.model.PasswordResetToken token = new com.medicore.api.model.PasswordResetToken(user);
        tokenRepository.save(token);

        String resetLink = "http://localhost:3000/reset-password?token=" + token.getToken();
        
        // Resolve phone number (User -> Doctor/Nurse/Patient fallback)
        String phone = user.getPhone();
        if (phone == null || phone.isEmpty()) {
            if (user.getRole() == User.Role.DOCTOR) {
                phone = doctorRepository.findByUser(user).map(d -> d.getPhone()).orElse(null);
            } else if (user.getRole() == User.Role.NURSE) {
                phone = nurseRepository.findByUser(user).map(n -> n.getPhone()).orElse(null);
            } else if (user.getRole() == User.Role.PATIENT) {
                phone = patientRepository.findByUser(user).map(p -> p.getContact()).orElse(null);
            } else if (user.getRole() == User.Role.LABTECH) {
                phone = labTechnicianRepository.findByUser(user).map(l -> l.getPhone()).orElse(null);
            }
        }

        // Send via WhatsApp API
        if (phone != null && !phone.isEmpty()) {
            String message = "MediCore Clinical Access Recovery:\n\nPlease use the link below to set your new security key:\n" + resetLink + "\n\n(Expires in 1 hour)";
            whatsappService.sendMessage(phone, message);
        }
        
        activityLogger.log("LockIcon", "Password reset initiated for: " + user.getName(), user.getName());

        String maskedPhone = "********";
        if (user.getPhone() != null && user.getPhone().length() > 6) {
            maskedPhone = user.getPhone().substring(0, 4) + "****" + user.getPhone().substring(user.getPhone().length() - 3);
        }

        return ResponseEntity.ok(Map.of(
            "status", "success", 
            "message", "Reset link dispatched",
            "phone", maskedPhone,
            "username", user.getUsername()
        ));
    }

    private ResponseEntity<?> resetPassword(String tokenStr, String newPassword) {
        if (tokenStr == null || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Token and new password are required"));
        }

        Optional<com.medicore.api.model.PasswordResetToken> tokenOpt = tokenRepository.findByToken(tokenStr);
        
        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Invalid or expired reset token"));
        }

        com.medicore.api.model.PasswordResetToken token = tokenOpt.get();
        User user = token.getUser();
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        tokenRepository.delete(token);
        
        activityLogger.log("LockIcon", "Password successfully reset for: " + user.getName(), user.getName());

        return ResponseEntity.ok(Map.of("status", "success", "message", "Password has been reset successfully"));
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
