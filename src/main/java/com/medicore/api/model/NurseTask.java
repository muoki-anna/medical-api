package com.medicore.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nurse_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NurseTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_code", unique = true)
    private String taskCode;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_time")
    private java.time.LocalTime dueTime;

    private String priority; // Low, Medium, High
    
    private String status = "todo"; // todo, inprogress, done

    @ManyToOne
    @JoinColumn(name = "assigned_nurse_id")
    private Nurse assignedNurse;
}
