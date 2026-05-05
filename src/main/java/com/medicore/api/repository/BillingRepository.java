package com.medicore.api.repository;

import com.medicore.api.model.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long> {
    @Query("SELECT SUM(b.amount) FROM Billing b WHERE b.status = 'Paid'")
    Double sumPaidBills();
}
