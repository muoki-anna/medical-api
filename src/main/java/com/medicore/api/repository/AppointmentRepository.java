package com.medicore.api.repository;

import com.medicore.api.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    long countByStatus(String status);
}
