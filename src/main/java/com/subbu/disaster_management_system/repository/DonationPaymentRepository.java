package com.subbu.disaster_management_system.repository;

import com.subbu.disaster_management_system.model.DonationPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonationPaymentRepository extends JpaRepository<DonationPayment, Long> {
}
