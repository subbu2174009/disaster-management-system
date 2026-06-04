package com.subbu.disaster_management_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisasterIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String incidentId;
    private String hazardType;
    private int severityScale;
    private boolean isActive;

    // Retained for Leaflet map mapping and details feed
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private LocalDateTime reportedAt;

    @ManyToOne
    @JoinColumn(name = "affected_zone_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("registeredIncidents")
    private AffectedZone affectedZone;

    public void triggerAlert() {
        this.isActive = true;
    }

    public void resolveIncident() {
        this.isActive = false;
    }

    public double calculateImpactRadius() {
        return severityScale * 1.5;
    }
}
