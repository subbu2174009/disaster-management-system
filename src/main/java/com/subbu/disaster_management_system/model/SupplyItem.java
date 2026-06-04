package com.subbu.disaster_management_system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "supply_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "reliefBatch")
public class SupplyItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stockKeepingUnitSKU;
    private String itemCategoryLabel;
    private double unitCostValuation;
    private int currentStockLevel;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    @JsonIgnore
    private ReliefBatch reliefBatch;

    public void displayItemMetrics() {
        System.out.printf("    SKU %s [%s] @%.2f x %d.%n", 
            stockKeepingUnitSKU, itemCategoryLabel, unitCostValuation, currentStockLevel);
    }

    public void deductInventoryStock(int quantity) {
        if (quantity <= currentStockLevel) {
            currentStockLevel -= quantity;
        }
    }

    public boolean triggerLowStockAlert() {
        return currentStockLevel < 10;
    }
}
