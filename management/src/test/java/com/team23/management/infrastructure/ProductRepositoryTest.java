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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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

    @Test
    @DisplayName("검색 조건 없을 때 모든 상품 (DELETED 제외) 반환")
    void searchNoConditionReturnsAll() {
        Product p1 = Product.create("티셔츠", Category.FASHION, 10000, "면", 1L);
        Product p2 = Product.create("청바지", Category.FASHION, 30000, "면", 1L);
        Product p3 = Product.create("삭제될_상품", Category.FOOD, 5000, "면", 1L);
        p3.delete();

        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);

        em.flush();
        em.clear();

        // when - 조건 없이 검색
        Page<Product> result = productRepository.search(
                null, null, PageRequest.of(0, 20)
        );

        // then - 2개 반환 (DELETED 제외)
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("상품명으로 부분 일치 검색")
    void searchByNamePartialMatch() {
        Product p1 = Product.create("면 티셔츠", Category.FASHION, 10000, "면", 1L);
        Product p2 = Product.create("면 양말", Category.FASHION, 30000, "면", 1L);
        Product p3 = Product.create("상품", Category.FOOD, 5000, "면", 1L);


        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);

        em.flush();
        em.clear();

        Page<Product> result = productRepository.search("면", null, PageRequest.of(0, 20));

        assertThat(result.getContent()).extracting(Product::getName).containsExactlyInAnyOrder("면 티셔츠", "면 양말");
    }

    @Test
    @DisplayName("카테고리로 검색")
    void searchByCategoryExactMatch() {
        Product p1 = Product.create("면 티셔츠", Category.FASHION, 10000, "면", 1L);
        Product p2 = Product.create("면 양말", Category.FASHION, 30000, "면", 1L);
        Product p3 = Product.create("상품", Category.FOOD, 5000, "면", 1L);


        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);

        em.flush();
        em.clear();

        Page<Product> result = productRepository.search(null, Category.FASHION, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(2);

    }

    @Test
    @DisplayName("상품명 + 카테고리 동시 검색")
    void searchByNameAndCategory() {
        Product p1 = Product.create("면 티셔츠", Category.FASHION, 10000, "면", 1L);
        Product p2 = Product.create("면 양말", Category.FASHION, 30000, "면", 1L);
        Product p3 = Product.create("양말", Category.FASHION, 30000, "면", 1L);
        Product p4 = Product.create("상품", Category.FOOD, 5000, "면", 1L);


        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);
        productRepository.save(p4);

        em.flush();
        em.clear();

        Page<Product> result = productRepository.search("양말", Category.FASHION, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("페이징 동작")
    void searchWithPaging() {
        Product p1 = Product.create("면 티셔츠", Category.FASHION, 10000, "면", 1L);
        Product p2 = Product.create("면 양말", Category.FASHION, 30000, "면", 1L);
        Product p3 = Product.create("양말", Category.FASHION, 30000, "면", 1L);
        Product p4 = Product.create("상품1", Category.FOOD, 5000, "면", 1L);
        Product p5 = Product.create("상품2", Category.FOOD, 5000, "면", 1L);


        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);
        productRepository.save(p4);
        productRepository.save(p5);

        em.flush();
        em.clear();

        Page<Product> result = productRepository.search("", null, PageRequest.of(0, 2));

        assertThat(result.getContent()).size().isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("정렬 동작")
    void searchWithSort() {
        Product p1 = Product.create("면 티셔츠", Category.FASHION, 10000, "면", 1L);
        Product p2 = Product.create("면 양말", Category.FASHION, 20000, "면", 1L);
        Product p3 = Product.create("양말", Category.FASHION, 30000, "면", 1L);


        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);

        em.flush();
        em.clear();

        Page<Product> result = productRepository.search("", null, PageRequest.of(0, 20, Sort.by("basePrice").descending()));

        assertThat(result.getContent()).extracting(Product::getName).containsExactly("양말", "면 양말", "면 티셔츠");
    }
}