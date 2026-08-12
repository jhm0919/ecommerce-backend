package com.shop.admin.product.service;

import com.shop.admin.product.dto.ProductAdminCreateRequest;
import com.shop.admin.product.dto.ProductAdminUpdateRequest;
import com.shop.admin.product.dto.SkuAdminAddRequest;
import com.shop.admin.product.dto.SkuAdminResponse;
import com.shop.cart.repository.CartRepository;
import com.shop.category.domain.Category;
import com.shop.category.exception.CategoryNotFoundException;
import com.shop.category.repository.CategoryRepository;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.domain.SkuOption;
import com.shop.product.dto.ProductDetailResponse;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAdminService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartRepository cartRepository;

    /**
     * 새 상품을 등록한다.
     * 재고는 SKU 추가 후 SKU 단위로 관리된다.
     */
    @Transactional
    public ProductDetailResponse register(ProductAdminCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        Product product = Product.register(
                request.name(),
                request.price(),
                request.description(),  // ★ stock 제거
                request.mainImageUrl(),
                category
        );

        Product savedProduct = productRepository.save(product);
        return ProductDetailResponse.from(savedProduct);
    }

    /**
     * 상품 정보를 수정한다 (PATCH 의미).
     */
    @Transactional
    public ProductDetailResponse update(Long productId, ProductAdminUpdateRequest request) {
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

    /**
     * 상품을 단종 처리한다 (Soft Delete).
     */
    @Transactional
    public void discontinue(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.discontinue();

        //  모든 장바구니에서 해당 상품 아이템을 삭제
        cartRepository.deleteAllItemsByProductId(productId);
        log.info("해당 상품이 장바구니에서 삭제되었습니다.: productId={}", productId);
    }

    /**
     * 상품에 SKU를 추가한다.
     */
    @Transactional
    public SkuAdminResponse addSku(Long productId, SkuAdminAddRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // sku의 옵션 이름, 옵션 값들
        List<SkuOption> options = request.options().stream()
                .map(opt -> new SkuOption(opt.name(), opt.value()))
                .toList();

        // 1. sku 추가
        Sku newSku = product.addSku(options, request.initialStock());

        productRepository.save(product);

        return SkuAdminResponse.from(newSku);
    }

    /**
     * SKU를 제거한다 (재고 0인 경우만).
     */
    @Transactional
    public void removeSku(Long productId, Long skuId) {
        Product product = productRepository.findByIdWithSkus(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.removeSku(skuId);
        log.info("SKU removed: productId={}, skuId={}", productId, skuId);
    }
}
