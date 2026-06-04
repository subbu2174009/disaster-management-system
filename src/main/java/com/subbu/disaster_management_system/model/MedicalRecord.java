package com.subbu.disaster_management_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chartRecordId;
    private String trackingVictimId;
    private String bloodType;

    @OneToOne
    @JoinColumn(name = "victim_id")
    private Victim victim;

    // Stores text log of diagnoses, prescriptions, and treatments
    @Column(length = 8000)
    private String historyLog;

    public void appendChartEntry(String type, String details) {
        String currentLog = this.historyLog;
        if (currentLog == null || currentLog.trim().isEmpty() || currentLog.equals("[]")) {
            currentLog = "";
        } else {
            currentLog = currentLog + "\n";
        }
        String newEntry = "[" + type.toUpperCase() + "] " + details;
        this.historyLog = currentLog + newEntry;
    }

    public String extractLongitudinalMedicalHistory() {
        return this.historyLog;
    }
}
