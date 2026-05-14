package com.medicore.api.controller;

import com.medicore.api.model.Activity;
import com.medicore.api.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ActivityController {

    @Autowired
    private ActivityRepository activityRepository;

    @GetMapping("/activities")
    public ResponseEntity<?> getActivities() {
        List<Activity> activities = activityRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .toList();
        return ResponseEntity.ok(Map.of("status", "success", "data", activities));
    }

    @PostMapping("/activities")
    public ResponseEntity<?> createActivity(@RequestBody Map<String, String> body) {
        Activity activity = new Activity();
        activity.setIcon(body.getOrDefault("icon", "ActivityIcon"));
        activity.setDescription(body.get("description"));
        activity.setPatientName(body.get("patientName"));
        activity.setActionDate(java.time.LocalDate.now());
        activityRepository.save(activity);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Activity logged"));
    }
}
