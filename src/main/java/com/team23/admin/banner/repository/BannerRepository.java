package com.team23.admin.banner.repository;

import com.team23.admin.banner.domain.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    boolean existsByDisplayOrder(int displayOrder);
    boolean existsByDisplayOrderAndIdNot(int displayOrder, Long id);
    List<Banner> findAllByOrderByDisplayOrderAsc();

}
