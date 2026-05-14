package com.medicore.api.controller;

import com.medicore.api.model.NurseTask;
import com.medicore.api.model.Patient;
import com.medicore.api.repository.NurseTaskRepository;
import com.medicore.api.repository.PatientRepository;
import com.medicore.api.repository.NurseRepository;
import com.medicore.api.util.ActivityLogger;
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
    private PatientRepository patientRepository;

    @Autowired
    private NurseRepository nurseRepository;

    @Autowired
    private NurseTaskRepository nurseTaskRepository;

    @Autowired
    private ActivityLogger activityLogger;

    @GetMapping("/nurse-tasks")
    public ResponseEntity<?> getTasks(@RequestParam(required = false) String nurseId) {
        System.out.println("[NurseTaskController] Fetching tasks for nurseId: " + nurseId);
        List<NurseTask> tasks;
        try {
            if (nurseId != null && !nurseId.isEmpty() && !nurseId.equals("undefined")) {
                Long id = Long.parseLong(nurseId);
                // Try finding by Nurse entity ID first, then by User ID
                tasks = nurseTaskRepository.findByAssignedNurseUserId(id);
                if (tasks.isEmpty()) {
                    tasks = nurseTaskRepository.findByAssignedNurseId(id);
                }
            } else {
                tasks = nurseTaskRepository.findAll();
            }
        } catch (Exception e) {
            System.err.println("[NurseTaskController] Filter error: " + e.getMessage());
            tasks = nurseTaskRepository.findAll();
        }

        System.out.println("[NurseTaskController] Found " + tasks.size() + " tasks");

        List<Map<String, Object>> result = tasks.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("taskCode", t.getTaskCode());
            m.put("patient", t.getPatient() != null ? t.getPatient().getName() : "General Protocol");
            m.put("description", t.getDescription());
            m.put("dueTime", t.getDueTime() != null ? t.getDueTime().toString() : "ASAP");
            m.put("priority", t.getPriority());
            // Sync status and column for frontend compatibility
            String status = t.getStatus() != null ? t.getStatus() : "todo";
            m.put("status", status);
            m.put("column", status); 
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
                Object p = body.get("patient");
                if (p instanceof Map) {
                    Map<?, ?> pm = (Map<?, ?>) p;
                    if (pm.containsKey("id")) {
                        patientRepository.findById(Long.parseLong(pm.get("id").toString())).ifPresent(task::setPatient);
                    }
                } else {
                    String name = p.toString();
                    patientRepository.findAll().stream()
                        .filter(pat -> pat.getName().equalsIgnoreCase(name))
                        .findFirst()
                        .ifPresent(task::setPatient);
                }
            }

            // Handle Nurse Assignment
            if (body.containsKey("nurseId") && body.get("nurseId") != null) {
                Long uId = Long.parseLong(body.get("nurseId").toString());
                // Try finding nurse by User ID first, then by Nurse ID
                nurseRepository.findByUserId(uId)
                    .ifPresentOrElse(task::setAssignedNurse, () -> {
                        nurseRepository.findById(uId).ifPresent(task::setAssignedNurse);
                    });
            } else if (body.containsKey("assignedNurse") && body.get("assignedNurse") != null) {
                Object n = body.get("assignedNurse");
                if (n instanceof Map) {
                    Map<?, ?> nm = (Map<?, ?>) n;
                    if (nm.containsKey("id")) {
                        nurseRepository.findById(Long.parseLong(nm.get("id").toString())).ifPresent(task::setAssignedNurse);
                    }
                }
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
            
            activityLogger.log(
                "ActivityIcon",
                "Clinical task updated: [" + task.getStatus() + "] " + task.getDescription() + " for " + (task.getPatient() != null ? task.getPatient().getName() : "General Ward"),
                task.getPatient() != null ? task.getPatient().getName() : "General Ward"
            );

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
