package com.medicore.api.repository;

import com.medicore.api.model.LabTechnician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabTechnicianRepository extends JpaRepository<LabTechnician, Long> {
    java.util.Optional<com.medicore.api.model.LabTechnician> findByUser(com.medicore.api.model.User user);
}
