package com.medicore.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_code", unique = true)
    private String testCode;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "test_type")
    private String testType;

    private String urgency; // Routine, STAT

    @Column(name = "date_requested")
    private LocalDate dateRequested;

    @Column(name = "date_completed")
    private LocalDate dateCompleted;

    private String status = "pending"; // pending, in-progress, completed

    @Column(columnDefinition = "TEXT")
    private String result;

    private String flag; // normal, abnormal

    @ManyToOne
    @JoinColumn(name = "technician_id")
    private LabTechnician technician;

    @Column(name = "reference_range", columnDefinition = "TEXT")
    private String referenceRange;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
