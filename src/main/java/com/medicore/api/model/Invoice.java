package com.medicore.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber;
    private LocalDate date;
    private String itemDescription;
    private Double amount;
    private String status; // paid, pending, cancelled

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;
}
