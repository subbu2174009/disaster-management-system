package com.subbu.disaster_management_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "allocation_invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceId;
    private String associatedTargetZoneId;
    private double aggregatedOperationalCost;
    private String approvalSignature; // PENDING / SIGNED / CLEARED
    private String settlementMethod;


    @OneToOne
    @JoinColumn(name = "batch_id")
    private ReliefBatch reliefBatch;

    @ManyToOne
    @JoinColumn(name = "donation_id")
    private DonationPayment donationPayment;

    public void finaliseInvoiceCalculation() {
        this.approvalSignature = "SIGNED";
    }

    public void applyForGovernmentSubsidy() {
        double subsidy = this.aggregatedOperationalCost * 0.40;
        this.aggregatedOperationalCost -= subsidy;
    }

    public boolean auditExpenditures() {
        return true;
    }
}
