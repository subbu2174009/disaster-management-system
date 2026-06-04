package com.subbu.disaster_management_system.controller;

import com.subbu.disaster_management_system.model.AllocationInvoice;
import com.subbu.disaster_management_system.model.ReliefBatch;
import com.subbu.disaster_management_system.model.SupplyItem;
import com.subbu.disaster_management_system.repository.AllocationInvoiceRepository;
import com.subbu.disaster_management_system.repository.ReliefBatchRepository;
import com.subbu.disaster_management_system.repository.SupplyItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/logistics")
@CrossOrigin(origins = "*")
public class LogisticsController {

    @Autowired
    private ReliefBatchRepository reliefBatchRepository;

    @Autowired
    private SupplyItemRepository supplyItemRepository;

    @Autowired
    private AllocationInvoiceRepository allocationInvoiceRepository;

    // ----- BATCH ENDPOINTS -----

    @GetMapping("/batches")
    public List<ReliefBatch> getAllBatches() {
        return reliefBatchRepository.findAll();
    }

    @PostMapping("/batches")
    public ReliefBatch createBatch(@RequestBody ReliefBatch batch) {
        if (batch.getCargoBatchId() == null) {
            batch.setCargoBatchId("BATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (batch.getDeliveryStatus() == null) {
            batch.setDeliveryStatus("PENDING");
        }
        return reliefBatchRepository.save(batch);
    }

    @PutMapping("/batches/{id}/status")
    public ResponseEntity<ReliefBatch> updateBatchStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        return reliefBatchRepository.findById(id)
                .map(batch -> {
                    batch.setDeliveryStatus(status);
                    if ("SHIPPED".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status)) {
                        batch.dispatchBatch();
                    }
                    return ResponseEntity.ok(reliefBatchRepository.save(batch));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Modify: Add item to a batch
    @PostMapping("/batches/{id}/items")
    public ResponseEntity<ReliefBatch> addItemToBatch(
            @PathVariable Long id, 
            @RequestBody SupplyItem item) {
        Optional<ReliefBatch> batchOpt = reliefBatchRepository.findById(id);
        if (batchOpt.isEmpty()) return ResponseEntity.notFound().build();

        ReliefBatch batch = batchOpt.get();
        item.setReliefBatch(batch);
        supplyItemRepository.save(item);

        return ResponseEntity.ok(reliefBatchRepository.findById(id).get());
    }

    // Modify: Delete item from a batch
    @DeleteMapping("/batches/{id}/items/{itemId}")
    public ResponseEntity<ReliefBatch> deleteItemFromBatch(
            @PathVariable Long id, 
            @PathVariable Long itemId) {
        Optional<ReliefBatch> batchOpt = reliefBatchRepository.findById(id);
        if (batchOpt.isEmpty()) return ResponseEntity.notFound().build();

        Optional<SupplyItem> itemOpt = supplyItemRepository.findById(itemId);
        if (itemOpt.isEmpty()) return ResponseEntity.notFound().build();

        SupplyItem item = itemOpt.get();
        if (item.getReliefBatch() != null && item.getReliefBatch().getId().equals(id)) {
            supplyItemRepository.delete(item);
        }

        return ResponseEntity.ok(reliefBatchRepository.findById(id).get());
    }

    // Generate Invoice from Relief Batch
    @PostMapping("/batches/{id}/invoice")
    public ResponseEntity<?> generateInvoiceFromBatch(
            @PathVariable Long id, 
            @RequestParam String zoneId) {
        Optional<ReliefBatch> batchOpt = reliefBatchRepository.findById(id);
        if (batchOpt.isEmpty()) return ResponseEntity.notFound().build();

        ReliefBatch batch = batchOpt.get();
        double totalCost = batch.calculateTotalCost();

        AllocationInvoice invoice = AllocationInvoice.builder()
                .invoiceId("INV-" + batch.getCargoBatchId() + "-" + System.currentTimeMillis() % 1000)
                .associatedTargetZoneId(zoneId)
                .aggregatedOperationalCost(totalCost)
                .approvalSignature("PENDING")
                .reliefBatch(batch)
                .build();

        AllocationInvoice savedInvoice = allocationInvoiceRepository.save(invoice);
        return ResponseEntity.ok(savedInvoice);
    }

    // ----- SUPPLY ITEMS LEDGER -----

    @GetMapping("/supplies")
    public List<SupplyItem> getAllSupplies() {
        return supplyItemRepository.findAll();
    }

    @PostMapping("/supplies")
    public SupplyItem addSupplyItem(@RequestBody SupplyItem item) {
        return supplyItemRepository.save(item);
    }

    @PutMapping("/supplies/{id}/deduct")
    public ResponseEntity<?> deductStock(
            @PathVariable Long id, 
            @RequestParam int quantity) {
        return supplyItemRepository.findById(id)
                .map(item -> {
                    if (item.getCurrentStockLevel() < quantity) {
                        return ResponseEntity.badRequest().body("Insufficient stock levels for SKU " + item.getStockKeepingUnitSKU());
                    }
                    item.deductInventoryStock(quantity);
                    return ResponseEntity.ok(supplyItemRepository.save(item));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
