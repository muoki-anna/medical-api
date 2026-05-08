package com.medicore.api.repository;

import com.medicore.api.model.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long> {
    List<LabTest> findByPatientId(Long patientId);
    List<LabTest> findByDoctorId(Long doctorId);
    long countByStatus(String status);
}
