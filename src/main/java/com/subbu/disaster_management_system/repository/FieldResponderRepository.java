package com.subbu.disaster_management_system.repository;

import com.subbu.disaster_management_system.model.FieldResponder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldResponderRepository extends JpaRepository<FieldResponder, Long> {
    List<FieldResponder> findByOperationalStatus(String status);
}
