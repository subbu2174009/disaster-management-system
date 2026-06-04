package com.subbu.disaster_management_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "donation_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionalPaymentId;
    private double clearFundsAmount;
    private String routingChannelApproach;
    private boolean anonymousDonorFlag;
    private LocalDateTime timestamp; // retained for transaction chronological log order

    public void authorizationHandshake() {
        // Validation log or flag clearing check
    }

    public void refundTransaction() {
        this.clearFundsAmount = 0.0;
    }

    public void issueTaxExemptionReceipt() {
        // Can output receipt or update receipt status flag
    }
}
