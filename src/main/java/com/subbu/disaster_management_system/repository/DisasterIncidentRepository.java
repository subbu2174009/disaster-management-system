package com.subbu.disaster_management_system.repository;

import com.subbu.disaster_management_system.model.DisasterIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisasterIncidentRepository extends JpaRepository<DisasterIncident, Long> {
    List<DisasterIncident> findByIsActive(boolean isActive);
}
