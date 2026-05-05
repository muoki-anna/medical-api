package com.medicore.api.repository;

import com.medicore.api.model.ReportArtifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportArtifactRepository extends JpaRepository<ReportArtifact, Long> {
}
