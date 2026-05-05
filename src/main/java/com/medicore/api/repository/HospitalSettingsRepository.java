package com.medicore.api.repository;

import com.medicore.api.model.HospitalSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalSettingsRepository extends JpaRepository<HospitalSettings, Long> {
}
