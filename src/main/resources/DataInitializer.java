package com.subbu.disaster_management_system.config;

import com.subbu.disaster_management_system.model.*;
import com.subbu.disaster_management_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private ShelterRepository shelterRepository;

    @Autowired
    private FacilityRoomRepository facilityRoomRepository;

    @Autowired
    private VictimRepository victimRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private ReliefBatchRepository reliefBatchRepository;

    @Autowired
    private SupplyItemRepository supplyItemRepository;

    @Autowired
    private DonationPaymentRepository donationPaymentRepository;

    @Autowired
    private AllocationInvoiceRepository allocationInvoiceRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // 1. Incidents
        if (incidentRepository.count() == 0) {
            Incident i1 = Incident.builder()
                    .title("Flooding in Downtown Manhattan")
                    .type(Incident.IncidentType.FLOOD)
                    .latitude(40.7060)
                    .longitude(-74.0080)
                    .severity(Incident.SeverityLevel.HIGH)
                    .description("Heavy storm surge has breached the seawall. Lower level subway lines flooding. Evacuations in progress.")
                    .status(Incident.IncidentStatus.ACTIVE)
                    .reportedAt(LocalDateTime.now().minusHours(3))
                    .build();

            Incident i2 = Incident.builder()
                    .title("Brooklyn Transformer Explosion")
                    .type(Incident.IncidentType.OTHER)
                    .latitude(40.6782)
                    .longitude(-73.9442)
                    .severity(Incident.SeverityLevel.MEDIUM)
                    .description("Substation failure caused local blackout. Fire departments responding. Grid recovery expected in 4 hours.")
                    .status(Incident.IncidentStatus.REPORTED)
                    .reportedAt(LocalDateTime.now().minusMinutes(45))
                    .build();

            Incident i3 = Incident.builder()
                    .title("Central Park Brush Fire")
                    .type(Incident.IncidentType.WILDFIRE)
                    .latitude(40.7850)
                    .longitude(-73.9683)
                    .severity(Incident.SeverityLevel.CRITICAL)
                    .description("Dry, windy conditions causing fire to spread rapidly in the Ramble. Citizens advised to avoid the park entirely.")
                    .status(Incident.IncidentStatus.ACTIVE)
                    .reportedAt(LocalDateTime.now().minusHours(1))
                    .build();

            Incident i4 = Incident.builder()
                    .title("Minor Tremor - Flushing")
                    .type(Incident.IncidentType.EARTHQUAKE)
                    .latitude(40.7282)
                    .longitude(-73.7949)
                    .severity(Incident.SeverityLevel.LOW)
                    .description("Magnitude 3.2 tremor felt. Structural damage checks complete. No casualties or critical damage reported.")
                    .status(Incident.IncidentStatus.RESOLVED)
                    .reportedAt(LocalDateTime.now().minusDays(1))
                    .build();

            incidentRepository.saveAll(Arrays.asList(i1, i2, i3, i4));
        }

        // 2. Shelters & Rooms
        if (shelterRepository.count() == 0) {
            
            // Manhattan Shelter
            Shelter s1 = Shelter.builder()
                    .name("Manhattan Emergency Shelter")
                    .latitude(40.7150)
                    .longitude(-74.0030)
                    .address("123 Broadway, NYC")
                    .contactInfo("+1 (555) 019-2831")
                    .rooms(new ArrayList<>())
                    .build();
            shelterRepository.save(s1);

            FacilityRoom r101 = FacilityRoom.builder().roomNo(101).capacity(2).occupied(0).specializedMedical(true).shelter(s1).build();
            FacilityRoom r102 = FacilityRoom.builder().roomNo(102).capacity(2).occupied(0).specializedMedical(false).shelter(s1).build();
            facilityRoomRepository.saveAll(Arrays.asList(r101, r102));
            
            s1.getRooms().addAll(Arrays.asList(r101, r102));
            s1.updateCapacityStats();
            shelterRepository.save(s1);

            // Brooklyn Shelter
            Shelter s2 = Shelter.builder()
                    .name("Brooklyn Safety Haven")
                    .latitude(40.6900)
                    .longitude(-73.9800)
                    .address("45 Flatbush Ave, Brooklyn")
                    .contactInfo("+1 (555) 019-4822")
                    .rooms(new ArrayList<>())
                    .build();
            shelterRepository.save(s2);

            FacilityRoom r201 = FacilityRoom.builder().roomNo(201).capacity(3).occupied(0).specializedMedical(false).shelter(s2).build();
            FacilityRoom r202 = FacilityRoom.builder().roomNo(202).capacity(3).occupied(0).specializedMedical(true).shelter(s2).build();
            facilityRoomRepository.saveAll(Arrays.asList(r201, r202));

            s2.getRooms().addAll(Arrays.asList(r201, r202));
            s2.updateCapacityStats();
            shelterRepository.save(s2);

            // Queens Shelter
            Shelter s3 = Shelter.builder()
                    .name("Queens Relief Center")
                    .latitude(40.7500)
                    .longitude(-73.8800)
                    .address("78 Astoria Blvd, Queens")
                    .contactInfo("+1 (555) 019-9911")
                    .rooms(new ArrayList<>())
                    .build();
            shelterRepository.save(s3);

            FacilityRoom r301 = FacilityRoom.builder().roomNo(301).capacity(5).occupied(0).specializedMedical(false).shelter(s3).build();
            FacilityRoom r302 = FacilityRoom.builder().roomNo(302).capacity(5).occupied(0).specializedMedical(false).shelter(s3).build();
            facilityRoomRepository.saveAll(Arrays.asList(r301, r302));

            s3.getRooms().addAll(Arrays.asList(r301, r302));
            s3.updateCapacityStats();
            shelterRepository.save(s3);
            
            // 3. Victims & Housed Allocations
            Victim v1 = Victim.builder()
                    .name("Rahul")
                    .age(45)
                    .gender("M")
                    .phoneNumber("88880001")
                    .triageStatus(1)
                    .medicalAlertFlag(true)
                    .specialNeeds("Dehydration and shock")
                    .shelter(s1)
                    .facilityRoom(r101)
                    .build();

            Victim v2 = Victim.builder()
                    .name("Meena")
                    .age(27)
                    .gender("F")
                    .phoneNumber("88880002")
                    .triageStatus(2)
                    .medicalAlertFlag(false)
                    .specialNeeds("insulin tracking")
                    .shelter(s1)
                    .facilityRoom(r101)
                    .build();

            victimRepository.saveAll(Arrays.asList(v1, v2));

            // Occupy rooms for v1 and v2
            r101.setOccupied(2);
            facilityRoomRepository.save(r101);
            s1.updateCapacityStats();
            shelterRepository.save(s1);

            // 4. Clinical Medical Charts
            MedicalRecord m1 = MedicalRecord.builder()
                    .victim(v1)
                    .bloodType("O+")
                    .historyLog("[DIAGNOSIS] ICD-FLOOD-01: hypothermia + dehydration\n[Rx] RX-1: Antibiotic IV (8h x 5d)\n[TREATMENT] IV rehydration cost=$800.00")
                    .build();

            MedicalRecord m2 = MedicalRecord.builder()
                    .victim(v2)
                    .bloodType("A-")
                    .historyLog("[DIAGNOSIS] Mild Shock\n[TREATMENT] Rest and psychological support")
                    .build();

            medicalRecordRepository.saveAll(Arrays.asList(m1, m2));
        }

        // 5. Logistics: Relief Batches & Supply Items
        if (reliefBatchRepository.count() == 0) {
            
            // Batch 1 (Pending pull-request)
            ReliefBatch b1 = ReliefBatch.builder()
                    .batchCode("BATCH-ALPHA")
                    .sourceNode("Central Warehouse")
                    .status(ReliefBatch.BatchStatus.PENDING)
                    .build();
            reliefBatchRepository.save(b1);

            SupplyItem w1 = SupplyItem.builder().sku("SKU-WATER-1L").category("Water").unitCost(12.0).stockLevel(500).reliefBatch(b1).build();
            SupplyItem m1 = SupplyItem.builder().sku("SKU-MED-KIT").category("Medical").unitCost(450.0).stockLevel(8).reliefBatch(b1).build();
            supplyItemRepository.saveAll(Arrays.asList(w1, m1));

            // Batch 2 (Shipped / Approved)
            ReliefBatch b2 = ReliefBatch.builder()
                    .batchCode("BATCH-BETA")
                    .sourceNode("East Supply Hub")
                    .status(ReliefBatch.BatchStatus.APPROVED)
                    .build();
            reliefBatchRepository.save(b2);

            SupplyItem s1 = SupplyItem.builder().sku("SKU-BLANKET").category("Shelter").unitCost(25.0).stockLevel(50).reliefBatch(b2).build();
            supplyItemRepository.save(s1);
            
            // 6. Finance: Donations & Allocation Invoices
            DonationPayment d1 = DonationPayment.builder()
                    .amount(5000.0)
                    .channel("SWIFT-WIRE")
                    .anonymous(false)
                    .timestamp(LocalDateTime.now().minusDays(2))
                    .build();

            DonationPayment d2 = DonationPayment.builder()
                    .amount(1000.0)
                    .channel("CREDIT-CARD")
                    .anonymous(true)
                    .timestamp(LocalDateTime.now().minusHours(4))
                    .build();
            donationPaymentRepository.saveAll(Arrays.asList(d1, d2));

            // Invoice for BATCH-BETA (50 blankets * $25 = $1250)
            AllocationInvoice inv = AllocationInvoice.builder()
                    .invoiceCode("INV-BATCH-BETA")
                    .zoneId("Z-NORTH")
                    .operationalCost(1250.0)
                    .status(AllocationInvoice.InvoiceStatus.PENDING)
                    .reliefBatch(b2)
                    .build();
            allocationInvoiceRepository.save(inv);
        }
    }
}
