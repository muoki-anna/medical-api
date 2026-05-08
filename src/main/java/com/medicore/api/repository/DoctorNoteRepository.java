package com.medicore.api.repository;

import com.medicore.api.model.DoctorNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DoctorNoteRepository extends JpaRepository<DoctorNote, Long> {
    List<DoctorNote> findByPatientId(Long patientId);
    List<DoctorNote> findByDoctorId(Long doctorId);
}
