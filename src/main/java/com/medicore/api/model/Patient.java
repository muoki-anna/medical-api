package com.medicore.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_code", unique = true)
    private String patientCode;

    @Column(nullable = false)
    private String name;

    private Integer age;

    @Column(name = "gender")
    private String gender;

    private LocalDate dob;

    @Column(name = "blood_type")
    private String bloodType;

    private String diagnosis;

    @ManyToOne
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @ManyToOne
    @JoinColumn(name = "assigned_doctor_id")
    private Doctor assignedDoctor;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    private String status = "outpatient";

    private String contact;
    private String email;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "emergency_phone")
    private String emergencyPhone;

    @Column(name = "insurance_provider")
    private String insuranceProvider;

    @Column(name = "policy_number")
    private String policyNumber;

    @Column(name = "insurance_expiry")
    private LocalDate insuranceExpiry;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
