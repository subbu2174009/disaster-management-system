package com.subbu.disaster_management_system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "facility_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "shelter")
public class FacilityRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int roomNo;
    private int capacity; // keeps for max limit reference
    private int currentBedVacancyCount;
    private boolean specializedMedicalSetup;
    private boolean isolationProtocolActive;

    @ManyToOne
    @JoinColumn(name = "shelter_id")
    @JsonIgnore
    private Shelter shelter;

    public void occupySpace() {
        if (currentBedVacancyCount > 0) {
            currentBedVacancyCount--;
        }
    }

    public void vacateSpace() {
        if (currentBedVacancyCount < capacity) {
            currentBedVacancyCount++;
        }
    }

    public void toggleIsolationProtocol() {
        this.isolationProtocolActive = !this.isolationProtocolActive;
    }
}
