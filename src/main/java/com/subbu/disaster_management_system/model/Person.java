package com.subbu.disaster_management_system.model;

import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Person {
    protected String personId;
    protected String name;
    protected int age;
    protected String gender;
    protected String phoneNumber;
}
