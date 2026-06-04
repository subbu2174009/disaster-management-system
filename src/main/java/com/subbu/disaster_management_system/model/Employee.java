package com.subbu.disaster_management_system.model;

import jakarta.persistence.MappedSuperclass;
import lombok.*;
import java.time.LocalDateTime;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class Employee extends Person {

    protected String empId;
    protected double salary;
    protected LocalDateTime deploymentDate;
    protected String operationalStatus; // ACTIVE / STANDBY / OFF-DUTY

    public Employee(String personId, String name, int age, String gender, String phoneNumber,
                    String empId, double salary, String operationalStatus) {
        super(personId, name, age, gender, phoneNumber);
        this.empId = empId;
        this.salary = salary;
        this.deploymentDate = LocalDateTime.now();
        this.operationalStatus = operationalStatus;
    }

    public double calculateHazardPayBonus() {
        double factor = "ACTIVE".equalsIgnoreCase(operationalStatus) ? 0.30 : 0.10;
        return salary * factor;
    }
}
