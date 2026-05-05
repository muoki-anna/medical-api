package com.medicore.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hospital_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hospitalName = "MediCore General Hospital";
    private String address = "Nairobi, Kenya — Kenyatta Avenue, P.O. Box 12345";
    private String contactNumber = "020 123 4567";
    private String email = "info@medicore.ke";
    
    private Boolean emailNotifications = true;
    private Boolean smsNotifications = false;
    private Boolean auditLog = true;
}
