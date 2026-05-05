package com.medicore.api.controller;

import com.medicore.api.model.HospitalSettings;
import com.medicore.api.repository.HospitalSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SettingsController {

    @Autowired
    private HospitalSettingsRepository settingsRepository;

    @GetMapping("/settings")
    public ResponseEntity<?> getSettings() {
        HospitalSettings settings = settingsRepository.findAll().stream().findFirst().orElse(new HospitalSettings());
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", settings);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody HospitalSettings settings) {
        HospitalSettings existing = settingsRepository.findAll().stream().findFirst().orElse(new HospitalSettings());
        settings.setId(existing.getId());
        HospitalSettings saved = settingsRepository.save(settings);
        return ResponseEntity.ok(Map.of("status", "success", "data", saved));
    }
}
