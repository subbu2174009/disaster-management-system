package com.subbu.disaster_management_system.config;

import com.subbu.disaster_management_system.model.*;
import com.subbu.disaster_management_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DisasterIncidentRepository incidentRepository;

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

    @Autowired
    private FieldResponderRepository responderRepository;

    @Autowired
    private EmergencyAssignmentRepository assignmentRepository;

    @Autowired
    private AffectedZoneRepository zoneRepository;

    @Override
    public void run(String... args) throws Exception {
        
        // 0. Seed Affected Zones
        AffectedZone zNorth = null;
        AffectedZone zEast = null;
        if (zoneRepository.count() == 0) {
            zNorth = AffectedZone.builder()
                    .zoneId("Z-NORTH")
                    .quadrantGeocode("40.7060,-74.0080")
                    .casualtyCount(42)
                    .housesDamaged(15)
                    .livesLost(3)
                    .evacuationRequestCount(12)
                    .infrastructureRiskScale(8)
                    .evacuationRequired(true)
                    .build();
            zEast = AffectedZone.builder()
                    .zoneId("Z-EAST")
                    .quadrantGeocode("40.6782,-73.9442")
                    .casualtyCount(18)
                    .housesDamaged(4)
                    .livesLost(0)
                    .evacuationRequestCount(2)
                    .infrastructureRiskScale(5)
                    .evacuationRequired(false)
                    .build();
            zoneRepository.saveAll(Arrays.asList(zNorth, zEast));
        } else {
            zNorth = zoneRepository.findByZoneId("Z-NORTH").orElse(null);
            zEast = zoneRepository.findByZoneId("Z-EAST").orElse(null);
        }

        // 1. Incidents
        if (incidentRepository.count() == 0) {
            DisasterIncident i1 = DisasterIncident.builder()
                    .incidentId("INC-FLOOD-01")
                    .hazardType("FLOOD")
                    .severityScale(8)
                    .isActive(true)
                    .title("Flooding in Downtown Manhattan")
                    .latitude(40.7060)
                    .longitude(-74.0080)
                    .description("Heavy storm surge has breached the seawall. Lower level subway lines flooding. Evacuations in progress.")
                    .reportedAt(LocalDateTime.now().minusHours(3))
                    .affectedZone(zNorth)
                    .build();

            DisasterIncident i2 = DisasterIncident.builder()
                    .incidentId("INC-EXPL-02")
                    .hazardType("OTHER")
                    .severityScale(5)
                    .isActive(true)
                    .title("Brooklyn Transformer Explosion")
                    .latitude(40.6782)
                    .longitude(-73.9442)
                    .description("Substation failure caused local blackout. Fire departments responding. Grid recovery expected in 4 hours.")
                    .reportedAt(LocalDateTime.now().minusMinutes(45))
                    .affectedZone(zEast)
                    .build();

            DisasterIncident i3 = DisasterIncident.builder()
                    .incidentId("INC-FIRE-03")
                    .hazardType("WILDFIRE")
                    .severityScale(9)
                    .isActive(true)
                    .title("Central Park Brush Fire")
                    .latitude(40.7850)
                    .longitude(-73.9683)
                    .description("Dry, windy conditions causing fire to spread rapidly in the Ramble. Citizens advised to avoid the park entirely.")
                    .reportedAt(LocalDateTime.now().minusHours(1))
                    .affectedZone(zNorth)
                    .build();

            DisasterIncident i4 = DisasterIncident.builder()
                    .incidentId("INC-QUAKE-04")
                    .hazardType("EARTHQUAKE")
                    .severityScale(3)
                    .isActive(false)
                    .title("Minor Tremor - Flushing")
                    .latitude(40.7282)
                    .longitude(-73.7949)
                    .description("Magnitude 3.2 tremor felt. Structural damage checks complete. No casualties or critical damage reported.")
                    .reportedAt(LocalDateTime.now().minusDays(1))
                    .affectedZone(zEast)
                    .build();

            incidentRepository.saveAll(Arrays.asList(i1, i2, i3, i4));
        }

        // 2. Shelters & Rooms
        if (shelterRepository.count() == 0) {
            
            // Manhattan Shelter
            Shelter s1 = Shelter.builder()
                    .shelterId("SH-01")
                    .name("Manhattan Emergency Shelter")
                    .latitude(40.7150)
                    .longitude(-74.0030)
                    .structuralAddress("123 Broadway, NYC")
                    .contactInfo("+1 (555) 019-2831")
                    .maxCapacity(4)
                    .safetyRating("A")
                    .rooms(new ArrayList<>())
                    .build();
            shelterRepository.save(s1);

            FacilityRoom r101 = FacilityRoom.builder().roomNo(101).capacity(2).currentBedVacancyCount(0).specializedMedicalSetup(true).shelter(s1).build();
            FacilityRoom r102 = FacilityRoom.builder().roomNo(102).capacity(2).currentBedVacancyCount(2).specializedMedicalSetup(false).shelter(s1).build();
            facilityRoomRepository.saveAll(Arrays.asList(r101, r102));
            
            s1.getRooms().addAll(Arrays.asList(r101, r102));
            shelterRepository.save(s1);

            // Brooklyn Shelter
            Shelter s2 = Shelter.builder()
                    .shelterId("SH-02")
                    .name("Brooklyn Safety Haven")
                    .latitude(40.6900)
                    .longitude(-73.9800)
                    .structuralAddress("45 Flatbush Ave, Brooklyn")
                    .contactInfo("+1 (555) 019-4822")
                    .maxCapacity(6)
                    .safetyRating("A")
                    .rooms(new ArrayList<>())
                    .build();
            shelterRepository.save(s2);

            FacilityRoom r201 = FacilityRoom.builder().roomNo(201).capacity(3).currentBedVacancyCount(3).specializedMedicalSetup(false).shelter(s2).build();
            FacilityRoom r202 = FacilityRoom.builder().roomNo(202).capacity(3).currentBedVacancyCount(3).specializedMedicalSetup(true).shelter(s2).build();
            facilityRoomRepository.saveAll(Arrays.asList(r201, r202));

            s2.getRooms().addAll(Arrays.asList(r201, r202));
            shelterRepository.save(s2);

            // Queens Shelter
            Shelter s3 = Shelter.builder()
                    .shelterId("SH-03")
                    .name("Queens Relief Center")
                    .latitude(40.7500)
                    .longitude(-73.8800)
                    .structuralAddress("78 Astoria Blvd, Queens")
                    .contactInfo("+1 (555) 019-9911")
                    .maxCapacity(10)
                    .safetyRating("B")
                    .rooms(new ArrayList<>())
                    .build();
            shelterRepository.save(s3);

            FacilityRoom r301 = FacilityRoom.builder().roomNo(301).capacity(5).currentBedVacancyCount(5).specializedMedicalSetup(false).shelter(s3).build();
            FacilityRoom r302 = FacilityRoom.builder().roomNo(302).capacity(5).currentBedVacancyCount(5).specializedMedicalSetup(false).shelter(s3).build();
            facilityRoomRepository.saveAll(Arrays.asList(r301, r302));

            s3.getRooms().addAll(Arrays.asList(r301, r302));
            shelterRepository.save(s3);
            
            // 3. Victims & Housed Allocations
            Victim v1 = Victim.builder()
                    .victimId("VIC-001")
                    .triageStatus("Tier 1 - Critical")
                    .medicalAlertFlag(true)
                    .specializedNeedsLog("Dehydration and shock")
                    .shelter(s1)
                    .facilityRoom(r101)
                    .build();
            v1.setPersonId("P-VICTOR-1");
            v1.setName("Rahul");
            v1.setAge(45);
            v1.setGender("M");
            v1.setPhoneNumber("88880001");

            Victim v2 = Victim.builder()
                    .victimId("VIC-002")
                    .triageStatus("Tier 2 - Urgent Care")
                    .medicalAlertFlag(false)
                    .specializedNeedsLog("insulin tracking")
                    .shelter(s1)
                    .facilityRoom(r101)
                    .build();
            v2.setPersonId("P-VICTOR-2");
            v2.setName("Meena");
            v2.setAge(27);
            v2.setGender("F");
            v2.setPhoneNumber("88880002");

            victimRepository.saveAll(Arrays.asList(v1, v2));

            // 4. Clinical Medical Charts
            MedicalRecord m1 = MedicalRecord.builder()
                    .victim(v1)
                    .chartRecordId("MR-001")
                    .trackingVictimId(v1.getVictimId())
                    .bloodType("O+")
                    .historyLog("[DIAGNOSIS] ICD-FLOOD-01: hypothermia + dehydration\n[Rx] RX-1: Antibiotic IV (8h x 5d)\n[TREATMENT] IV rehydration cost=$800.00")
                    .build();

            MedicalRecord m2 = MedicalRecord.builder()
                    .victim(v2)
                    .chartRecordId("MR-002")
                    .trackingVictimId(v2.getVictimId())
                    .bloodType("A-")
                    .historyLog("[DIAGNOSIS] Mild Shock\n[TREATMENT] Rest and psychological support")
                    .build();

            medicalRecordRepository.saveAll(Arrays.asList(m1, m2));
        }

        // 4.5. Seed Field Responders
        FieldResponder fr1 = null;
        FieldResponder fr2 = null;
        if (responderRepository.count() == 0) {
            fr1 = FieldResponder.builder()
                    .personId("P-FR-SANTHOSH")
                    .name("Santhosh")
                    .age(31)
                    .gender("M")
                    .phoneNumber("99991111")
                    .empId("E-FR-1")
                    .salary(5500.0)
                    .operationalStatus("ACTIVE")
                    .tacticalSpecialty("Water Rescue")
                    .certificationLevel(3)
                    .currentGpsCoordinates("40.7060,-74.0080")
                    .build();

            fr2 = FieldResponder.builder()
                    .personId("P-FR-SUBHASH")
                    .name("Subhash")
                    .age(28)
                    .gender("M")
                    .phoneNumber("99992222")
                    .empId("E-FR-2")
                    .salary(4800.0)
                    .operationalStatus("STANDBY")
                    .tacticalSpecialty("Triage Medicine")
                    .certificationLevel(4)
                    .currentGpsCoordinates("40.6782,-73.9442")
                    .build();

            responderRepository.saveAll(Arrays.asList(fr1, fr2));
        } else {
            List<FieldResponder> list = responderRepository.findAll();
            if (list.size() >= 2) {
                fr1 = list.get(0);
                fr2 = list.get(1);
            }
        }

        // 4.6. Seed Emergency Assignments
        if (assignmentRepository.count() == 0 && fr1 != null) {
            DisasterIncident activeIncident = incidentRepository.findAll().stream()
                    .filter(DisasterIncident::isActive)
                    .findFirst()
                    .orElse(null);
            
            if (activeIncident != null) {
                EmergencyAssignment asg = EmergencyAssignment.builder()
                        .assignmentId("ASG-DEPL-ALPHA")
                        .operationalShift("Day Shift")
                        .activationTimestamp(LocalDateTime.now().minusHours(2))
                        .isCompleted(false)
                        .fieldResponder(fr1)
                        .disasterIncident(activeIncident)
                        .fieldIncidentNotes("[]")
                        .build();
                asg.logFieldIncidentNotes("First aid kits deployed. Evacuations in block 4 completed.");
                assignmentRepository.save(asg);
            }
        }

        // 5. Logistics: Relief Cargo Batches & Supply Items
        if (reliefBatchRepository.count() == 0) {
            
            // Batch 1 (Pending pull-request)
            ReliefBatch b1 = ReliefBatch.builder()
                    .cargoBatchId("BATCH-ALPHA")
                    .logisticsSourceNode("Central Warehouse")
                    .deliveryStatus("PENDING")
                    .build();
            reliefBatchRepository.save(b1);

            SupplyItem w1 = SupplyItem.builder().stockKeepingUnitSKU("SKU-WATER-1L").itemCategoryLabel("Water").unitCostValuation(12.0).currentStockLevel(500).reliefBatch(b1).build();
            SupplyItem m1 = SupplyItem.builder().stockKeepingUnitSKU("SKU-MED-KIT").itemCategoryLabel("Medical").unitCostValuation(450.0).currentStockLevel(8).reliefBatch(b1).build();
            supplyItemRepository.saveAll(Arrays.asList(w1, m1));

            // Batch 2 (Shipped / Approved)
            ReliefBatch b2 = ReliefBatch.builder()
                    .cargoBatchId("BATCH-BETA")
                    .logisticsSourceNode("East Supply Hub")
                    .deliveryStatus("APPROVED")
                    .build();
            reliefBatchRepository.save(b2);

            SupplyItem s1 = SupplyItem.builder().stockKeepingUnitSKU("SKU-BLANKET").itemCategoryLabel("Shelter").unitCostValuation(25.0).currentStockLevel(50).reliefBatch(b2).build();
            supplyItemRepository.save(s1);
            
            // 6. Finance: Donations & Allocation Invoices
            DonationPayment d1 = DonationPayment.builder()
                    .transactionalPaymentId("PAY-100")
                    .clearFundsAmount(5000.0)
                    .routingChannelApproach("SWIFT-WIRE")
                    .anonymousDonorFlag(false)
                    .timestamp(LocalDateTime.now().minusDays(2))
                    .build();

            DonationPayment d2 = DonationPayment.builder()
                    .transactionalPaymentId("PAY-101")
                    .clearFundsAmount(1000.0)
                    .routingChannelApproach("CREDIT-CARD")
                    .anonymousDonorFlag(true)
                    .timestamp(LocalDateTime.now().minusHours(4))
                    .build();
            donationPaymentRepository.saveAll(Arrays.asList(d1, d2));

            // Invoice for BATCH-BETA (50 blankets * $25 = $1250)
            AllocationInvoice inv = AllocationInvoice.builder()
                    .invoiceId("INV-BATCH-BETA")
                    .associatedTargetZoneId("Z-NORTH")
                    .aggregatedOperationalCost(1250.0)
                    .approvalSignature("PENDING")
                    .reliefBatch(b2)
                    .build();
            allocationInvoiceRepository.save(inv);
        }
    }
}
