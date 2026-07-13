package com.team23.seller.repository;

import com.team23.seller.domain.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
}
