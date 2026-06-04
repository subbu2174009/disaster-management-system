package com.subbu.disaster_management_system.repository;

import com.subbu.disaster_management_system.model.AllocationInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllocationInvoiceRepository extends JpaRepository<AllocationInvoice, Long> {
}
