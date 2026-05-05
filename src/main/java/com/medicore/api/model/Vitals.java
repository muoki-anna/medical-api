package com.medicore.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "vitals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vitals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private String bp;
    private Integer hr;
    private Double temp;
    private Integer spo2;
    private Integer rr;
    private Double weight;

    @Column(name = "measured_at")
    private LocalDateTime measuredAt = LocalDateTime.now();
}
