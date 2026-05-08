package com.medicore.api.repository;

import com.medicore.api.model.Nurse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medicore.api.model.User;
import java.util.Optional;

@Repository
public interface NurseRepository extends JpaRepository<Nurse, Long> {
    Optional<Nurse> findByUser(User user);
    Optional<Nurse> findByUserId(Long userId);
}
