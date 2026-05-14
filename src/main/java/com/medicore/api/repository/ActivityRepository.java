package com.medicore.api.repository;

import com.medicore.api.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findTop10ByOrderByTimestampDesc();
    Activity findTopByOrderByIdDesc();
}
