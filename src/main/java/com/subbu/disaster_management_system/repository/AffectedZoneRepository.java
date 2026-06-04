package com.subbu.disaster_management_system.repository;

import com.subbu.disaster_management_system.model.AffectedZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AffectedZoneRepository extends JpaRepository<AffectedZone, Long> {
    Optional<AffectedZone> findByZoneId(String zoneId);
}
