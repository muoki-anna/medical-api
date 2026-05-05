package com.medicore.api.controller;

import com.medicore.api.model.NurseTask;
import com.medicore.api.model.Patient;
import com.medicore.api.repository.NurseTaskRepository;
import com.medicore.api.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class NurseTaskController {

    @Autowired
    private NurseTaskRepository nurseTaskRepository;

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/nurse-tasks")
    public ResponseEntity<?> getTasks(@RequestParam(required = false) Long nurseId) {
        List<NurseTask> tasks;
        if (nurseId != null) {
            tasks = nurseTaskRepository.findByAssignedNurseId(nurseId);
        } else {
            tasks = nurseTaskRepository.findAll();
        }

        List<Map<String, Object>> result = tasks.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("taskCode", t.getTaskCode());
            m.put("patient", t.getPatient() != null ? t.getPatient().getName() : "Unknown");
            m.put("description", t.getDescription());
            m.put("dueTime", t.getDueTime() != null ? t.getDueTime().toString() : "ASAP");
            m.put("priority", t.getPriority());
            m.put("column", t.getStatus()); // Keep 'column' key for frontend Kanban compatibility
            m.put("assignedNurse", t.getAssignedNurse() != null ? t.getAssignedNurse().getName() : "Unassigned");
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("status", "success", "data", result));
    }

    @PostMapping("/nurse-tasks")
    public ResponseEntity<?> saveTask(@RequestBody Map<String, Object> body) {
        try {
            NurseTask task;
            if (body.containsKey("id") && body.get("id") != null) {
                task = nurseTaskRepository.findById(Long.parseLong(body.get("id").toString())).orElse(new NurseTask());
            } else {
                task = new NurseTask();
                if (!body.containsKey("taskCode")) {
                    task.setTaskCode("NT-" + System.currentTimeMillis() % 1000000);
                }
            }

            if (body.containsKey("taskCode")) task.setTaskCode(body.get("taskCode").toString());

            if (body.containsKey("patientId") && body.get("patientId") != null) {
                Patient patient = patientRepository.findById(Long.parseLong(body.get("patientId").toString())).orElse(null);
                task.setPatient(patient);
            } else if (body.containsKey("patient") && body.get("patient") != null) {
                String name = body.get("patient").toString();
                patientRepository.findAll().stream()
                    .filter(p -> p.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .ifPresent(task::setPatient);
            }

            task.setDescription(body.getOrDefault("description", "No description").toString());
            
            Object dueTimeObj = body.get("dueTime");
            if (dueTimeObj != null) {
                try {
                    task.setDueTime(LocalTime.parse(dueTimeObj.toString()));
                } catch (Exception e) {
                    task.setDueTime(LocalTime.now().plusHours(1));
                }
            }

            task.setPriority(body.getOrDefault("priority", "Medium").toString());
            
            // Map 'column' from frontend to 'status' in backend
            if (body.containsKey("column")) {
                task.setStatus(body.get("column").toString());
            } else if (body.containsKey("status")) {
                task.setStatus(body.get("status").toString());
            }

            nurseTaskRepository.save(task);
            return ResponseEntity.ok(Map.of("status", "success", "data", task));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", "Processing error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/nurse-tasks")
    public ResponseEntity<?> deleteTask(@RequestParam Long id) {
        nurseTaskRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Task deleted"));
    }
}
