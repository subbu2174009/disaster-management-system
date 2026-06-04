package com.subbu.disaster_management_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "field_responders")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FieldResponder extends Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tacticalSpecialty;
    private int certificationLevel;
    private String currentGpsCoordinates;

    @Builder
    public FieldResponder(String personId, String name, int age, String gender, String phoneNumber,
                          String empId, double salary, String operationalStatus,
                          String tacticalSpecialty, int certificationLevel, String currentGpsCoordinates) {
        super(personId, name, age, gender, phoneNumber, empId, salary, operationalStatus);
        this.tacticalSpecialty = tacticalSpecialty;
        this.certificationLevel = certificationLevel;
        this.currentGpsCoordinates = currentGpsCoordinates;
    }

    public void displayRoleDetails() {
        System.out.printf("[FieldResponder] ID: %s, Specialty: %s, Status: %s%n", empId, tacticalSpecialty, operationalStatus);
    }
}
