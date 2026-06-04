package com.subbu.disaster_management_system.controller;

import com.subbu.disaster_management_system.model.DisasterIncident;
import com.subbu.disaster_management_system.repository.DisasterIncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin(origins = "*")
public class DisasterIncidentController {

    @Autowired
    private DisasterIncidentRepository incidentRepository;

    @GetMapping
    public List<DisasterIncident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    @PostMapping
    public DisasterIncident createIncident(@RequestBody DisasterIncident incident) {
        if (incident.getReportedAt() == null) {
            incident.setReportedAt(LocalDateTime.now());
        }
        if (incident.getIncidentId() == null) {
            incident.setIncidentId("INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        incident.setActive(true);
        return incidentRepository.save(incident);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DisasterIncident> updateIncidentStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        return incidentRepository.findById(id)
                .map(incident -> {
                    if ("RESOLVED".equalsIgnoreCase(status)) {
                        incident.resolveIncident();
                    } else {
                        incident.triggerAlert();
                    }
                    DisasterIncident updated = incidentRepository.save(incident);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
