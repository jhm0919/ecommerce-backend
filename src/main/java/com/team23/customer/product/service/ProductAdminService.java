package com.team23.customer.product.service;

import com.team23.customer.product.domain.*;
import com.team23.customer.product.dto.ProductCreateRequest;
import com.team23.customer.product.dto.ProductUpdateRequest;
import com.team23.customer.product.exception.CategoryNotFoundException;
import com.team23.customer.product.exception.ProductNotFoundException;
import com.team23.customer.product.repository.CategoryRepository;
import com.team23.customer.product.repository.ProductRepository;
import com.team23.customer.stockhistory.domain.StockChangeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAdminService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 새 상품을 등록한다.
     * 재고는 SKU 추가 후 SKU 단위로 관리된다.
     */
    @Transactional
    public Product register(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        Money price = new Money(request.price(), request.currency());

        Product product = Product.register(
                request.name(),
                price,
                request.description(),  // ★ stock 제거
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

        product.updateInfo(request.name(), request.description(), request.mainImageUrl());

        if (request.price() != null && request.currency() != null) {
            Money newPrice = new Money(request.price(), request.currency());
            product.changePrice(newPrice);
        } else if (request.price() != null || request.currency() != null) {
            throw new IllegalArgumentException(
                    "price and currency must be provided together");
        }

        if (request.categoryId() != null) {
            Category newCategory = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
            product.changeCategory(newCategory);
        }

        log.info("Product updated: id={}", productId);
        return product;
    }

    // ★ increaseStock(Long, int) 메서드 제거
    // → 재고는 SKU 단위로 관리 (increaseSkuStock 사용)

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

    /**
     * 상품에 SKU를 추가한다.
     */
    @Transactional
    public SKU addSku(Long productId, List<SkuOption> options, int initialStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        SKU sku = product.addSku(options, initialStock);

        eventPublisher.publishEvent(StockChangedEvent.of(
                product, sku,
                StockChangeType.SKU_CREATED,
                initialStock,
                0,            // stockBefore = 0 (새로 생성)
                initialStock, // stockAfter = initialStock
                null
        ));

        return sku;
    }

    /**
     * SKU 재고를 증가시킨다 (입고).
     */
    @Transactional
    public void increaseSkuStock(Long productId, Long skuId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        SKU sku = product.findSkuById(skuId).orElseThrow();
        int stockBefore = sku.getStock();

        product.increaseSkuStock(skuId, quantity);

        eventPublisher.publishEvent(StockChangedEvent.of(
                product, sku,
                StockChangeType.ADMIN_INCREASE,
                quantity,
                stockBefore,
                sku.getStock(),
                null  // orderId 없음
        ));
    }

    /**
     * SKU 재고를 감소시킨다 (수동 조정).
     */
    @Transactional
    public void decreaseSkuStock(Long productId, Long skuId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        SKU sku = product.findSkuById(skuId).orElseThrow();
        int stockBefore = sku.getStock();

        product.decreaseSkuStock(skuId, quantity);

        // 이력 이벤트
        eventPublisher.publishEvent(StockChangedEvent.of(
                product, sku,
                StockChangeType.ADMIN_DECREASE,
                quantity,
                stockBefore,
                sku.getStock(),
                null
        ));
    }

    /**
     * SKU를 제거한다 (재고 0인 경우만).
     */
    @Transactional
    public void removeSku(Long productId, Long skuId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.removeSku(skuId);
        log.info("SKU removed: productId={}, skuId={}", productId, skuId);
    }
}
