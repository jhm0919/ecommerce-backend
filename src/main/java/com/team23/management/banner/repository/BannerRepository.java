package com.team23.management.banner.repository;

import com.team23.management.banner.domain.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    boolean existsByDisplayOrder(int displayOrder);
    List<Banner> findAllByOrderByDisplayOrderAsc();

}
