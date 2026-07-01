package com.medicore.api.repository;

import com.medicore.api.model.LabTechnician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LabTechnicianRepository extends JpaRepository<LabTechnician, Long> {
    Optional<LabTechnician> findByUser(com.medicore.api.model.User user);
    Optional<LabTechnician> findByUserId(Long userId);
}
