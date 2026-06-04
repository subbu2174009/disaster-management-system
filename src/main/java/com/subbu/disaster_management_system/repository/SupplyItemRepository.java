package com.subbu.disaster_management_system.repository;

import com.subbu.disaster_management_system.model.SupplyItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupplyItemRepository extends JpaRepository<SupplyItem, Long> {
    List<SupplyItem> findByReliefBatchNull();
}
