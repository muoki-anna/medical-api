package com.medicore.api.repository;

import com.medicore.api.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import com.medicore.api.model.User;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByUser(User user);
}
