package com.medicore.api.repository;

import com.medicore.api.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByAssignedDoctorId(Long doctorId);
    List<Patient> findByNameContainingIgnoreCase(String name);
    Optional<Patient> findByUser(com.medicore.api.model.User user);
    Optional<Patient> findByUserId(Long userId);
}
