package com.subbu.disaster_management_system.controller;

import com.subbu.disaster_management_system.model.FacilityRoom;
import com.subbu.disaster_management_system.model.MedicalRecord;
import com.subbu.disaster_management_system.model.Shelter;
import com.subbu.disaster_management_system.model.Victim;
import com.subbu.disaster_management_system.repository.FacilityRoomRepository;
import com.subbu.disaster_management_system.repository.MedicalRecordRepository;
import com.subbu.disaster_management_system.repository.ShelterRepository;
import com.subbu.disaster_management_system.repository.VictimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/victims")
@CrossOrigin(origins = "*")
public class VictimController {

    @Autowired
    private VictimRepository victimRepository;

    @Autowired
    private ShelterRepository shelterRepository;

    @Autowired
    private FacilityRoomRepository facilityRoomRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @GetMapping
    public List<Victim> getAllVictims() {
        return victimRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> registerVictim(@RequestBody Victim victim) {
        Shelter allocatedShelter = null;
        FacilityRoom allocatedRoom = null;

        // Use selected room if provided
        if (victim.getFacilityRoom() != null && victim.getFacilityRoom().getId() != null) {
            Optional<FacilityRoom> roomOpt = facilityRoomRepository.findById(victim.getFacilityRoom().getId());
            if (roomOpt.isPresent()) {
                FacilityRoom r = roomOpt.get();
                if (r.getCurrentBedVacancyCount() > 0) {
                    allocatedRoom = r;
                    allocatedShelter = r.getShelter();
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Selected room is already full!");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Selected room not found!");
            }
        } else {
            // Auto-allocation fallback
            List<Shelter> shelters = shelterRepository.findAll();
            for (Shelter s : shelters) {
                for (FacilityRoom r : s.getRooms()) {
                    if (r.getCurrentBedVacancyCount() > 0) {
                        allocatedShelter = s;
                        allocatedRoom = r;
                        break;
                    }
                }
                if (allocatedRoom != null) break;
            }
        }

        if (allocatedRoom == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No vacant rooms available in any shelter! Housed capacity is full.");
        }

        // Occupy space using UML method
        allocatedRoom.occupySpace();
        facilityRoomRepository.save(allocatedRoom);
        shelterRepository.save(allocatedShelter);

        victim.setShelter(allocatedShelter);
        victim.setFacilityRoom(allocatedRoom);
        
        if (victim.getVictimId() == null) {
            victim.setVictimId("VIC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // Handle medical alert flag escalation
        if (victim.isMedicalAlertFlag()) {
            victim.flagCriticalMedicalAlert();
        } else if (victim.getTriageStatus() == null) {
            victim.setTriageStatus("Tier 3 - Stable");
        }

        // Person fields (name, age, gender, phoneNumber) are set by Jackson mapper automatically
        Victim savedVictim = victimRepository.save(victim);

        // Auto-create blank Medical Record
        MedicalRecord mr = MedicalRecord.builder()
                .victim(savedVictim)
                .chartRecordId("MR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .trackingVictimId(savedVictim.getVictimId())
                .bloodType("Unknown")
                .historyLog("[]")
                .build();
        medicalRecordRepository.save(mr);

        return ResponseEntity.ok(savedVictim);
    }

    @PutMapping("/{id}/triage")
    public ResponseEntity<Victim> updateTriage(@PathVariable Long id, @RequestParam String tier) {
        return victimRepository.findById(id)
                .map(v -> {
                    v.updateTriage(tier);
                    if ("Tier 1 - Critical".equalsIgnoreCase(tier)) {
                        v.setMedicalAlertFlag(true);
                    }
                    return ResponseEntity.ok(victimRepository.save(v));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/medical-record")
    public ResponseEntity<MedicalRecord> getMedicalRecord(@PathVariable Long id) {
        return medicalRecordRepository.findByVictimId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/medical-record/entry")
    public ResponseEntity<MedicalRecord> addMedicalEntry(
            @PathVariable Long id, 
            @RequestParam String type, 
            @RequestParam String details) {
        
        Optional<MedicalRecord> recordOpt = medicalRecordRepository.findByVictimId(id);
        if (recordOpt.isEmpty()) return ResponseEntity.notFound().build();

        MedicalRecord record = recordOpt.get();
        // Append entry using UML method
        record.appendChartEntry(type, details);
        
        return ResponseEntity.ok(medicalRecordRepository.save(record));
    }
}
