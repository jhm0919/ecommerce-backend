package com.team23.management.infrastructure;

import com.team23.management.domain.Sku.Sku;
import com.team23.management.domain.Sku.SkuOptionInput;
import com.team23.management.domain.Sku.SkuOptionValue;
import com.team23.management.domain.product.Category;
import com.team23.management.domain.product.Product;
import com.team23.management.domain.product.ProductStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SkuRepositoryTest {

    @Autowired
    SkuRepository skuRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("Sku 저장 확인")
    void saveThenFindById() {
        //given
        Sku sku = Sku.create(1L, List.of(
                        new SkuOptionInput("색상", "white"),
                        new SkuOptionInput("사이즈", "M")),
                0);

        //when
        Sku saved = skuRepository.save(sku);
        em.flush();
        em.clear();

        Sku found = skuRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getProductId()).isEqualTo(1L);
        assertThat(found.getOptions()).hasSize(2);
        assertThat(found.getOptions()).extracting(SkuOptionValue::getName).containsExactly("색상", "사이즈");
        assertThat(found.getOptions()).extracting(SkuOptionValue::getValue).containsExactly("white", "M");
        assertThat(found.getAdditionalPrice()).isEqualTo(0);
    }
}