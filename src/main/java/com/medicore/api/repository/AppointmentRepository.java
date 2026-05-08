package com.medicore.api.repository;

import com.medicore.api.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    long countByStatus(String status);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByPatientId(Long patientId);
}
