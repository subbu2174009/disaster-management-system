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
            @RequestParam String paymentMethod) {
        
        Optional<AllocationInvoice> invoiceOpt = allocationInvoiceRepository.findById(id);
        if (invoiceOpt.isEmpty()) return ResponseEntity.notFound().build();

        AllocationInvoice invoice = invoiceOpt.get();
        if ("CLEARED".equalsIgnoreCase(invoice.getApprovalSignature())) {
            return ResponseEntity.badRequest().body("Invoice is already cleared and settled.");
        }

        List<DonationPayment> donations = donationPaymentRepository.findAll();
        double totalTreasuryFunds = donations.stream().mapToDouble(DonationPayment::getClearFundsAmount).sum();
        
        if (totalTreasuryFunds < invoice.getAggregatedOperationalCost()) {
            return ResponseEntity.badRequest()
                    .body("Treasury balance ($" + totalTreasuryFunds 
                          + ") is insufficient to clear the operational cost ($" 
                          + invoice.getAggregatedOperationalCost() + ") of Invoice " + invoice.getInvoiceId());
        }

        // Deduct from the treasury pool
        double costToDeduct = invoice.getAggregatedOperationalCost();
        DonationPayment linkedDonation = null;

        for (DonationPayment donation : donations) {
            if (costToDeduct <= 0) break;
            
            double availableFunds = donation.getClearFundsAmount();
            if (availableFunds > 0) {
                if (linkedDonation == null) {
                    linkedDonation = donation;
                }
                
                if (availableFunds >= costToDeduct) {
                    donation.setClearFundsAmount(availableFunds - costToDeduct);
                    costToDeduct = 0;
                } else {
                    costToDeduct -= availableFunds;
                    donation.setClearFundsAmount(0.0);
                }
                donationPaymentRepository.save(donation);
            }
        }

        if (linkedDonation != null) {
            invoice.setDonationPayment(linkedDonation);
        }

        invoice.setSettlementMethod(paymentMethod);
        invoice.setApprovalSignature("CLEARED");
        
        AllocationInvoice saved = allocationInvoiceRepository.save(invoice);
        return ResponseEntity.ok(saved);
    }
}
