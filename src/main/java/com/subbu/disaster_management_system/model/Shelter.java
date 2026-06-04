package com.subbu.disaster_management_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shelters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shelter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shelterId;
    private String name; // retained for map popup/UI label
    private String structuralAddress;
    private int maxCapacity;
    private String safetyRating;
    private Double latitude;
    private Double longitude;
    private String contactInfo;

    @OneToMany(mappedBy = "shelter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<FacilityRoom> rooms = new ArrayList<>();

    public void addRoom(FacilityRoom r) {
        rooms.add(r);
        r.setShelter(this);
    }

    public double calculateCurrentOccupancyPercentage() {
        int freeBeds = 0;
        for (FacilityRoom r : rooms) {
            freeBeds += r.getCurrentBedVacancyCount();
        }
        int occupied = maxCapacity - freeBeds;
        if (maxCapacity == 0) return 0.0;
        return (occupied * 100.0) / maxCapacity;
    }

    public boolean verifyStructuralSafety() {
        return safetyRating != null && (safetyRating.equals("A") || safetyRating.equals("B"));
    }

    // Aggregates statistics for old UI endpoints if any
    public int getOccupied() {
        int freeBeds = 0;
        for (FacilityRoom r : rooms) {
            freeBeds += r.getCurrentBedVacancyCount();
        }
        return maxCapacity - freeBeds;
    }

    public int getCapacity() {
        return maxCapacity;
    }
}
