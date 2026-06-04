package com.subbu.disaster_management_system.repository;

import com.subbu.disaster_management_system.model.FacilityRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityRoomRepository extends JpaRepository<FacilityRoom, Long> {
}
