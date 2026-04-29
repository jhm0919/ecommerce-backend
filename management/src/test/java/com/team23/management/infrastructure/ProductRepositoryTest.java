package com.team23.management.infrastructure;

import com.team23.management.domain.product.Category;
import com.team23.management.domain.product.Product;
import com.team23.management.domain.product.ProductStatus;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("Product 저장 확인")
    void saveThenFindById() {
        //given
        Product product = Product.create(
                "면 티셔츠", Category.FASHION, 19000, "면", 1L
        );

        //when
        Product saved = productRepository.save(product);
        em.flush();
        em.clear();

        Product found = productRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getName()).isEqualTo("면 티셔츠");
        assertThat(found.getStatus()).isEqualTo(ProductStatus.ON_SALE);
    }
}