package com.subbu.disaster_management_system.controller;

import com.subbu.disaster_management_system.model.AllocationInvoice;
import com.subbu.disaster_management_system.model.DonationPayment;
import com.subbu.disaster_management_system.repository.AllocationInvoiceRepository;
import com.subbu.disaster_management_system.repository.DonationPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/finance")
@CrossOrigin(origins = "*")
public class FinanceController {

    @Autowired
    private DonationPaymentRepository donationPaymentRepository;

    @Autowired
    private AllocationInvoiceRepository allocationInvoiceRepository;

    @GetMapping("/donations")
    public List<DonationPayment> getAllDonations() {
        return donationPaymentRepository.findAll();
    }

    @PostMapping("/donations")
    public DonationPayment receiveDonation(@RequestBody DonationPayment donation) {
        if (donation.getTimestamp() == null) {
            donation.setTimestamp(LocalDateTime.now());
        }
        if (donation.getTransactionalPaymentId() == null) {
            donation.setTransactionalPaymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return donationPaymentRepository.save(donation);
    }

    @GetMapping("/invoices")
    public List<AllocationInvoice> getAllInvoices() {
        return allocationInvoiceRepository.findAll();
    }

    @PostMapping("/invoices/{id}/settle")
    public ResponseEntity<?> settleInvoice(
            @PathVariable Long id, 
            @RequestParam Long donationId) {
        
        Optional<AllocationInvoice> invoiceOpt = allocationInvoiceRepository.findById(id);
        if (invoiceOpt.isEmpty()) return ResponseEntity.notFound().build();

        Optional<DonationPayment> donationOpt = donationPaymentRepository.findById(donationId);
        if (donationOpt.isEmpty()) return ResponseEntity.badRequest().body("Donation transaction not found.");

        AllocationInvoice invoice = invoiceOpt.get();
        DonationPayment donation = donationOpt.get();

        // Business Rule Handshake: check if donation amount is sufficient
        if (donation.getClearFundsAmount() < invoice.getAggregatedOperationalCost()) {
            return ResponseEntity.badRequest()
                    .body("Donation amount ($" + donation.getClearFundsAmount() 
                          + ") is insufficient to clear the operational cost ($" 
                          + invoice.getAggregatedOperationalCost() + ") of Invoice " + invoice.getInvoiceId());
        }

        invoice.setApprovalSignature("CLEARED");
        invoice.setDonationPayment(donation);
        
        AllocationInvoice saved = allocationInvoiceRepository.save(invoice);
        return ResponseEntity.ok(saved);
    }
}
