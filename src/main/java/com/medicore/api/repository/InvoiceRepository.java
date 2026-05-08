package com.medicore.api.repository;

import com.medicore.api.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByPatientId(Long patientId);
}
