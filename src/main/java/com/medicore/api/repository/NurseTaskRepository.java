package com.medicore.api.repository;

import com.medicore.api.model.NurseTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NurseTaskRepository extends JpaRepository<NurseTask, Long> {
    List<NurseTask> findByAssignedNurseId(Long nurseId);
}
