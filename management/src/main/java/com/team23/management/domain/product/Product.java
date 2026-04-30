package com.team23.management.domain.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MIN_PRICE = 0;  // 0원 허용 (사은품/증정품 가능)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private int basePrice;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Column(nullable = false)
    private Long sellerId;

    public static Product create(
            String name, Category category, int basePrice,
            String description, Long sellerId
    ) {
        validateName(name);
        validatePrice(basePrice);
        validateCategory(category);
        Product product = new Product();
        product.name = name;
        product.category = category;
        product.basePrice = basePrice;
        product.description = description;
        product.sellerId = sellerId;
        product.status = ProductStatus.ON_SALE;
        return product;
    }

    public void delete() {
        this.status = ProductStatus.DELETED;
    }

    private static void validateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("카테고리를 선택해주세요.");
        }
    }

    private static void validatePrice(int basePrice) {
        if (basePrice < MIN_PRICE) {
            throw new IllegalArgumentException("가격은 0이하일 수 없습니다.");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("상품명은 100자 이하여야 합니다.");
        }
    }
}
