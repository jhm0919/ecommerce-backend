package com.shop.admin.product.service;

import com.shop.admin.product.dto.ProductCreateRequest;
import com.shop.admin.product.dto.ProductUpdateRequest;
import com.shop.admin.stockhistory.domain.StockChangeType;
import com.shop.cart.repository.CartRepository;
import com.shop.category.domain.Category;
import com.shop.category.exception.CategoryNotFoundException;
import com.shop.category.repository.CategoryRepository;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.domain.SkuOption;
import com.shop.product.domain.StockChangedEvent;
import com.shop.product.dto.ProductDetailResponse;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartRepository cartRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 새 상품을 등록한다.
     * 재고는 SKU 추가 후 SKU 단위로 관리된다.
     */
    @Transactional
    public Product register(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        Product product = Product.register(
                request.name(),
                request.price(),
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
    public ProductDetailResponse update(Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
        }

        product.update(request.name(),
                request.description(),
                request.mainImageUrl(),
                request.price(),
                category
        );

        log.info("Product updated: id={}", productId);
        return ProductDetailResponse.from(product);
    }

    // ★ increaseStock(Long, int) 메서드 제거
    // → 재고는 SKU 단위로 관리 (increaseSkuStock 사용)

    /**
     * 상품을 단종 처리한다 (Soft Delete).
     */
    @Transactional
    public void discontinue(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));

        product.discontinue();
        log.info("Product discontinued: id={}", productId);

        // 2. ★★★ 모든 장바구니에서 해당 상품 아이템을 삭제 ★★★
        cartRepository.deleteAllItemsByProductId(productId);
        log.info("해당 상품이 장바구니에서 삭제되었습니다.: productId={}", productId);

    }

    /**
     * 상품에 SKU를 추가한다.
     */
    @Transactional
    public Sku addSku(Long productId, List<SkuOption> options, int initialStock) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));

        // 1. SKU를 추가합니다.
        Sku newSku = product.addSku(options, initialStock);

        // 2. ★★★ 변경된 Product를 즉시 저장(save)하여 SKU의 ID를 생성받습니다. ★★★
        Product savedProduct = productRepository.save(product);

        // 3. 저장 후 반환된 Product 객체에서, ID가 부여된 SKU를 다시 찾습니다.
        //    (newSku의 skuCode는 고유하므로, 이를 이용해 찾을 수 있습니다.)
        Sku savedSku = savedProduct.getSkuses().stream()
                .filter(s -> s.getSkuCode().equals(newSku.getSkuCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to find saved SKU"));

        // 4. 이제 ID가 있는 savedSku 객체로 이벤트를 발행합니다.
        eventPublisher.publishEvent(StockChangedEvent.of(
                savedProduct, savedSku,
                StockChangeType.SKU_CREATED,
                initialStock,
                0,
                initialStock,
                null
        ));

        return savedSku;
    }

    /**
     * SKU 재고를 증가시킨다 (입고).
     */
    @Transactional
    public void increaseSkuStock(Long productId, Long skuId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Sku sku = product.findSkuById(skuId).orElseThrow();
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

        Sku sku = product.findSkuById(skuId).orElseThrow();
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
