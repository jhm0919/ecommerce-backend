package com.team23.management.infrastructure;

import com.team23.management.domain.product.Category;
import com.team23.management.domain.product.Product;
import com.team23.management.domain.product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE " +
            "p.status <> :excludedStatus AND " +
            "(:name IS NULL OR p.name LIKE CONCAT('%', :name, '%')) AND " +
            "(:category IS NULL OR p.category = :category)")
    Page<Product> searchInternal(
            @Param("excludedStatus") ProductStatus excludedStatus,
            @Param("name") String name,
            @Param("category") Category category,
            Pageable pageable
    );

    default Page<Product> search(String name, Category category, Pageable pageable) {
        return searchInternal(ProductStatus.DELETED, name, category, pageable);
    }
}
