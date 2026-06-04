package com.subbu.disaster_management_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "relief_batches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReliefBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cargoBatchId;
    private String logisticsSourceNode;
    private String deliveryStatus; // PACKED / IN-TRANSIT / DELIVERED / REJECTED

    @OneToMany(mappedBy = "reliefBatch", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private List<SupplyItem> items = new ArrayList<>();

    public void dispatchBatch() {
        this.deliveryStatus = "IN-TRANSIT";
    }

    public void updateDeliveryRoute(String checkpoint) {
        // Can be logged or recorded
    }

    public boolean verifyExpirationDates() {
        return true;
    }

    public double calculateTotalCost() {
        double cost = 0;
        for (SupplyItem s : items) {
            cost += (s.getUnitCostValuation() * s.getCurrentStockLevel());
        }
        return cost;
    }
}
