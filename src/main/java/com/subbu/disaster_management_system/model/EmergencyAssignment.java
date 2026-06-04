package com.subbu.disaster_management_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assignmentId;
    private String operationalShift; // Day Shift / Night Shift
    private LocalDateTime activationTimestamp;
    private boolean isCompleted;

    @Column(length = 4000)
    private String fieldIncidentNotes;

    @ManyToOne
    @JoinColumn(name = "responder_id")
    private FieldResponder fieldResponder;

    @ManyToOne
    @JoinColumn(name = "incident_id")
    private DisasterIncident disasterIncident;

    public void activateAssignment() {
        this.isCompleted = false;
        this.activationTimestamp = LocalDateTime.now();
    }

    public void terminateAssignment() {
        this.isCompleted = true;
    }

    public void logFieldIncidentNotes(String notes) {
        if (this.fieldIncidentNotes == null || this.fieldIncidentNotes.trim().isEmpty() || this.fieldIncidentNotes.equals("[]")) {
            this.fieldIncidentNotes = "";
        } else {
            this.fieldIncidentNotes = this.fieldIncidentNotes + "\n";
        }
        this.fieldIncidentNotes = this.fieldIncidentNotes + "[" + LocalDateTime.now() + "] " + notes;
    }
}
