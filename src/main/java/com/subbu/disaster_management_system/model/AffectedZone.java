package com.subbu.disaster_management_system.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "affected_zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffectedZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String zoneId;
    private String quadrantGeocode;
    private boolean evacuationRequired;
    private int casualtyCount;
    private int housesDamaged;
    private int livesLost;
    private int evacuationRequestCount;
    private int infrastructureRiskScale; // 1-10

    @OneToMany(mappedBy = "affectedZone", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    @JsonIgnoreProperties("affectedZone")
    private List<DisasterIncident> registeredIncidents = new ArrayList<>();

    public void addIncident(DisasterIncident i) {
        registeredIncidents.add(i);
        i.setAffectedZone(this);
    }

    public String assessEvacuationUrgency() {
        int maxSev = 0;
        for (DisasterIncident inc : registeredIncidents) {
            if (inc.getSeverityScale() > maxSev) {
                maxSev = inc.getSeverityScale();
            }
        }
        evacuationRequired = maxSev >= 6;
        return evacuationRequired ? "EVACUATE" : "monitor";
    }

    public void broadcastEmergencyNotification(String msg) {
        System.out.println("  [BROADCAST @" + quadrantGeocode + "] " + msg);
    }
}
