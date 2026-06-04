package com.subbu.disaster_management_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "victims")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Victim extends Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String victimId;
    private String triageStatus;
    private boolean medicalAlertFlag;
    private String specializedNeedsLog;

    @ManyToOne
    @JoinColumn(name = "shelter_id")
    private Shelter shelter;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private FacilityRoom facilityRoom;

    public void updateTriage(String tier) {
        this.triageStatus = tier;
    }

    public void logSpecializedNeeds(String needs) {
        this.specializedNeedsLog = needs;
    }

    public void flagCriticalMedicalAlert() {
        this.medicalAlertFlag = true;
        this.triageStatus = "Tier 1 - Critical";
    }
}
