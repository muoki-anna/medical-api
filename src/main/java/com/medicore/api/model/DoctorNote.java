package com.medicore.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "doctor_notes")
@Getter
@Setter
@NoArgsConstructor
public class DoctorNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "note_date")
    private LocalDate noteDate;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_private")
    private Boolean isPrivate = false;

    public DoctorNote(Long id, Patient patient, Doctor doctor, LocalDate noteDate, String content, Boolean isPrivate) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.noteDate = noteDate;
        this.content = content;
        this.isPrivate = isPrivate;
    }
}
