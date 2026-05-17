package com.team23.customer.seller.repository;

import com.team23.customer.seller.domain.ApplicationStatus;
import com.team23.customer.seller.domain.SellerApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerApplicationRepository extends JpaRepository<SellerApplication, Long> {
    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);
    boolean existsByMailOrderSalesNumber(String mailOrderSalesNumber);

    List<SellerApplication> findAllByStatus(ApplicationStatus status);
}
