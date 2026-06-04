package com.subbu.disaster_management_system.controller;

import com.subbu.disaster_management_system.model.DisasterIncident;
import com.subbu.disaster_management_system.model.EmergencyAssignment;
import com.subbu.disaster_management_system.model.FieldResponder;
import com.subbu.disaster_management_system.repository.DisasterIncidentRepository;
import com.subbu.disaster_management_system.repository.EmergencyAssignmentRepository;
import com.subbu.disaster_management_system.repository.FieldResponderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/personnel")
@CrossOrigin(origins = "*")
public class PersonnelController {

    @Autowired
    private FieldResponderRepository responderRepository;

    @Autowired
    private EmergencyAssignmentRepository assignmentRepository;

    @Autowired
    private DisasterIncidentRepository incidentRepository;

    // ----- RESPONDER ENDPOINTS -----

    @GetMapping("/responders")
    public List<FieldResponder> getAllResponders() {
        return responderRepository.findAll();
    }

    @PostMapping("/responders")
    public FieldResponder registerResponder(@RequestBody FieldResponder responder) {
        if (responder.getEmpId() == null) {
            responder.setEmpId("EMP-FR-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        }
        if (responder.getPersonId() == null) {
            responder.setPersonId("P-FR-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        }
        if (responder.getOperationalStatus() == null) {
            responder.setOperationalStatus("STANDBY");
        }
        if (responder.getCurrentGpsCoordinates() == null) {
            responder.setCurrentGpsCoordinates("40.7128,-74.0060");
        }
        return responderRepository.save(responder);
    }

    // ----- ASSIGNMENT ENDPOINTS -----

    @GetMapping("/assignments")
    public List<EmergencyAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    @PostMapping("/assignments")
    public ResponseEntity<?> createAssignment(@RequestBody EmergencyAssignment assignment) {
        if (assignment.getFieldResponder() == null || assignment.getFieldResponder().getId() == null) {
            return ResponseEntity.badRequest().body("Responder must be specified.");
        }
        if (assignment.getDisasterIncident() == null || assignment.getDisasterIncident().getId() == null) {
            return ResponseEntity.badRequest().body("Incident must be specified.");
        }

        Optional<FieldResponder> responderOpt = responderRepository.findById(assignment.getFieldResponder().getId());
        if (responderOpt.isEmpty()) return ResponseEntity.badRequest().body("Responder not found.");

        Optional<DisasterIncident> incidentOpt = incidentRepository.findById(assignment.getDisasterIncident().getId());
        if (incidentOpt.isEmpty()) return ResponseEntity.badRequest().body("Incident not found.");

        FieldResponder responder = responderOpt.get();
        DisasterIncident incident = incidentOpt.get();

        if ("ACTIVE".equalsIgnoreCase(responder.getOperationalStatus())) {
            return ResponseEntity.badRequest().body("Responder " + responder.getName() + " is already active on another deployment.");
        }

        // Activate and save
        responder.setOperationalStatus("ACTIVE");
        responderRepository.save(responder);

        assignment.setFieldResponder(responder);
        assignment.setDisasterIncident(incident);
        assignment.setAssignmentId("ASG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        assignment.activateAssignment();
        if (assignment.getFieldIncidentNotes() == null) {
            assignment.setFieldIncidentNotes("[]");
        }
        assignment.logFieldIncidentNotes("Deployment activated for " + responder.getName() + " on " + incident.getTitle());

        EmergencyAssignment saved = assignmentRepository.save(assignment);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/assignments/{id}/complete")
    public ResponseEntity<?> completeAssignment(@PathVariable Long id) {
        Optional<EmergencyAssignment> assignmentOpt = assignmentRepository.findById(id);
        if (assignmentOpt.isEmpty()) return ResponseEntity.notFound().build();

        EmergencyAssignment assignment = assignmentOpt.get();
        if (assignment.isCompleted()) {
            return ResponseEntity.badRequest().body("Assignment already completed.");
        }

        assignment.terminateAssignment();
        assignment.logFieldIncidentNotes("Deployment terminated. Standing down.");

        FieldResponder responder = assignment.getFieldResponder();
        if (responder != null) {
            responder.setOperationalStatus("STANDBY");
            responderRepository.save(responder);
        }

        EmergencyAssignment saved = assignmentRepository.save(assignment);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/assignments/{id}/notes")
    public ResponseEntity<?> appendNotes(
            @PathVariable Long id, 
            @RequestParam String notes) {
        
        Optional<EmergencyAssignment> assignmentOpt = assignmentRepository.findById(id);
        if (assignmentOpt.isEmpty()) return ResponseEntity.notFound().build();

        EmergencyAssignment assignment = assignmentOpt.get();
        assignment.logFieldIncidentNotes(notes);

        EmergencyAssignment saved = assignmentRepository.save(assignment);
        return ResponseEntity.ok(saved);
    }
}
