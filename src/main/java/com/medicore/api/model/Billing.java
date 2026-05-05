package com.medicore.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "billing")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Billing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_code", unique = true)
    private String billCode;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private String service;

    @Column(name = "billing_date")
    private LocalDate billingDate;

    private Double amount;
    private String status = "Pending"; // Paid, Pending, Partially Paid
}
