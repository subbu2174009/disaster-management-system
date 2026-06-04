package com.subbu.disaster_management_system.controller;

import com.subbu.disaster_management_system.model.AffectedZone;
import com.subbu.disaster_management_system.repository.AffectedZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/zones")
@CrossOrigin(origins = "*")
public class AffectedZoneController {

    @Autowired
    private AffectedZoneRepository zoneRepository;

    @GetMapping
    public List<AffectedZone> getAllZones() {
        return zoneRepository.findAll();
    }

    @PostMapping
    public AffectedZone createZone(@RequestBody AffectedZone zone) {
        if (zone.getZoneId() == null) {
            zone.setZoneId("ZONE-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        }
        if (zone.getQuadrantGeocode() == null) {
            zone.setQuadrantGeocode("40.7128,-74.0060");
        }
        return zoneRepository.save(zone);
    }

    @PutMapping("/{id}/metrics")
    public ResponseEntity<?> updateMetrics(
            @PathVariable Long id,
            @RequestParam(required = false) Integer casualties,
            @RequestParam(required = false) Integer damage,
            @RequestParam(required = false) Integer lives,
            @RequestParam(required = false) Integer evacuations,
            @RequestParam(required = false) Integer risk) {

        Optional<AffectedZone> zoneOpt = zoneRepository.findById(id);
        if (zoneOpt.isEmpty()) return ResponseEntity.notFound().build();

        AffectedZone zone = zoneOpt.get();
        if (casualties != null) zone.setCasualtyCount(casualties);
        if (damage != null) zone.setHousesDamaged(damage);
        if (lives != null) zone.setLivesLost(lives);
        if (evacuations != null) zone.setEvacuationRequestCount(evacuations);
        if (risk != null) zone.setInfrastructureRiskScale(risk);

        // Re-assess evacuation requirements
        zone.assessEvacuationUrgency();

        AffectedZone saved = zoneRepository.save(zone);
        return ResponseEntity.ok(saved);
    }
}
