package com.team23.customer.product.service;

import com.team23.customer.product.domain.Category;
import com.team23.customer.product.domain.Money;
import com.team23.customer.product.domain.Product;
import com.team23.customer.product.dto.ProductCreateRequest;
import com.team23.customer.product.dto.ProductUpdateRequest;
import com.team23.customer.product.exception.CategoryNotFoundException;
import com.team23.customer.product.exception.ProductNotFoundException;
import com.team23.customer.product.repository.CategoryRepository;
import com.team23.customer.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAdminService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 새 상품을 등록한다.
     */
    @Transactional
    public Product register(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        Money price = new Money(request.price(), request.currency());

        Product product = Product.register(
                request.name(),
                price,
                request.stock(),
                request.description(),
                request.mainImageUrl(),
                category
        );

        Product saved = productRepository.save(product);
        log.info("Product registered: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    /**
     * 상품 정보를 수정한다 (PATCH 의미).
     */
    @Transactional
    public Product update(Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // 1. 기본 정보 (name, description, image)
        product.updateInfo(request.name(), request.description(), request.mainImageUrl());

        // 2. 가격 (price와 currency 둘 다 있으면 변경)
        if (request.price() != null && request.currency() != null) {
            Money newPrice = new Money(request.price(), request.currency());
            product.changePrice(newPrice);
        } else if (request.price() != null || request.currency() != null) {
            throw new IllegalArgumentException(
                    "price and currency must be provided together");
        }

        // 3. 카테고리 변경
        if (request.categoryId() != null) {
            Category newCategory = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
            product.changeCategory(newCategory);
        }

        log.info("Product updated: id={}", productId);
        return product;
    }

    /**
     * 상품 재고를 증가시킨다 (입고).
     */
    @Transactional
    public Product increaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.increaseStock(quantity);
        log.info("Stock increased: id={}, quantity={}, newStock={}",
                productId, quantity, product.getStock());
        return product;
    }

    /**
     * 상품을 단종 처리한다 (Soft Delete).
     */
    @Transactional
    public void discontinue(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.discontinue();
        log.info("Product discontinued: id={}", productId);
    }
}