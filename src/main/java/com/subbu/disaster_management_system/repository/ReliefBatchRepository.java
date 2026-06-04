package com.subbu.disaster_management_system.repository;

import com.subbu.disaster_management_system.model.ReliefBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReliefBatchRepository extends JpaRepository<ReliefBatch, Long> {
}
