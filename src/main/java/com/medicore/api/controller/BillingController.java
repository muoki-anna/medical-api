package com.medicore.api.controller;

import com.medicore.api.model.Invoice;
import com.medicore.api.repository.InvoiceRepository;
import com.medicore.api.util.ActivityLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = "*")
public class BillingController {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ActivityLogger activityLogger;

    @GetMapping("/invoices")
    public ResponseEntity<?> getInvoices(@RequestParam Long patientId) {
        List<Invoice> invoices = invoiceRepository.findByPatientId(patientId);
        return ResponseEntity.ok(Map.of("status", "success", "data", invoices));
    }

    @PostMapping("/pay")
    public ResponseEntity<?> payInvoice(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Invoice invoice = invoiceRepository.findById(id).orElseThrow();
        invoice.setStatus("paid");
        invoiceRepository.save(invoice);
        
        activityLogger.log(
            "CheckCircleIcon",
            "Financial clearance: Invoice #" + invoice.getId() + " paid in full by " + (invoice.getPatient() != null ? invoice.getPatient().getName() : "Patient"),
            invoice.getPatient() != null ? invoice.getPatient().getName() : "Patient"
        );
        return ResponseEntity.ok(Map.of("status", "success", "message", "Payment processed successfully"));
    }
}
