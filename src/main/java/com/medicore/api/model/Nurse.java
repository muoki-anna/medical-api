package com.medicore.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nurses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nurse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private String name;
    private String email;
    private String phone;

    @ManyToOne
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @Enumerated(EnumType.STRING)
    private Shift shift;

    private String status = "active";

    public enum Shift {
        Morning, Evening, Night
    }
}
