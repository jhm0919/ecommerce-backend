package com.team23.customer.category.repository;

import com.team23.customer.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * slug로 카테고리 조회. URL 경로 처리에 사용.
     */
    Optional<Category> findBySlug(String slug);

    /**
     * 같은 이름의 카테고리가 이미 존재하는지 확인.
     */
    boolean existsByName(String name);

    /**
     * 같은 slug의 카테고리가 이미 존재하는지 확인.
     */
    boolean existsBySlug(String slug);
}