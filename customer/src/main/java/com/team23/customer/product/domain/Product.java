package com.team23.customer.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_status", columnList = "status"),
        @Index(name = "idx_product_category", columnList = "category_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "price_currency"))
    })
    private Money price;

    @Column(name = "category_id")
    private Long categoryId;  // ★ Category Aggregate ID 참조

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @ElementCollection
    @CollectionTable(
            name = "product_images",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @OrderColumn(name = "image_order")
    private List<ProductImage> images = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static Product create(String name, String description, Money price, Long categoryId) {
        Product product = new Product();
        product.name = name;
        product.description = description;
        product.price = price;
        product.categoryId = categoryId;
        product.status = ProductStatus.DRAFT;
        return product;
    }

    public void publish() {
        if (this.status != ProductStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT products can be published");
        }
        if (images.isEmpty()) {
            throw new IllegalStateException("Product must have at least one image");
        }
        this.status = ProductStatus.ACTIVE;
    }

    public void changePrice(Money newPrice) {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("Cannot change price of discontinued product");
        }
        this.price = newPrice;
    }

    public void markAsSoldOut() {
        this.status = ProductStatus.SOLD_OUT;
    }

    public void discontinue() {
        this.status = ProductStatus.DISCONTINUED;
    }

    public boolean isAvailableForOrder() {
        return this.status == ProductStatus.ACTIVE;
    }

    public void addImage(String url, String altText) {
        this.images.add(new ProductImage(url, altText));
    }
}
