package com.team23.customer.product.controller;

import com.team23.customer.product.domain.Product;
import com.team23.customer.product.domain.SKU;
import com.team23.customer.product.domain.SkuOption;
import com.team23.customer.product.dto.*;
import com.team23.customer.product.service.ProductAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductAdminService productAdminService;

    /**
     * 상품 등록.
     */
    @PostMapping
    public ResponseEntity<ProductDetailResponse> register(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        Product product = productAdminService.register(request);
        return ResponseEntity.status(201)
                .body(ProductDetailResponse.from(product));
    }

    /**
     * 상품 정보 수정.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        Product product = productAdminService.update(id, request);
        return ResponseEntity.ok(ProductDetailResponse.from(product));
    }

    // ★ restock 엔드포인트 제거
    // → 재고는 SKU 단위로 관리 (increaseSkuStock 사용)

    /**
     * 상품 단종 처리 (Soft Delete).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> discontinue(@PathVariable Long id) {
        productAdminService.discontinue(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * SKU 추가.
     */
    @PostMapping("/{productId}/skus")
    public ResponseEntity<SkuResponse> addSku(
            @PathVariable Long productId,
            @Valid @RequestBody AddSkuRequest request
    ) {
        List<SkuOption> options = request.options().stream()
                .map(opt -> new SkuOption(opt.name(), opt.value()))
                .toList();

        SKU sku = productAdminService.addSku(productId, options, request.initialStock());
        return ResponseEntity.status(201).body(SkuResponse.from(sku));
    }

    /**
     * SKU 재고 증가 (입고).
     */
    @PostMapping("/{productId}/skus/{skuId}/stock/increase")
    public ResponseEntity<Void> increaseSkuStock(
            @PathVariable Long productId,
            @PathVariable Long skuId,
            @Valid @RequestBody AdjustStockRequest request
    ) {
        productAdminService.increaseSkuStock(productId, skuId, request.quantity());
        return ResponseEntity.noContent().build();
    }

    /**
     * SKU 재고 감소 (수동 조정).
     */
    @PostMapping("/{productId}/skus/{skuId}/stock/decrease")
    public ResponseEntity<Void> decreaseSkuStock(
            @PathVariable Long productId,
            @PathVariable Long skuId,
            @Valid @RequestBody AdjustStockRequest request
    ) {
        productAdminService.decreaseSkuStock(productId, skuId, request.quantity());
        return ResponseEntity.noContent().build();
    }

    /**
     * SKU 제거 (재고 0인 경우만).
     */
    @DeleteMapping("/{productId}/skus/{skuId}")
    public ResponseEntity<Void> removeSku(
            @PathVariable Long productId,
            @PathVariable Long skuId
    ) {
        productAdminService.removeSku(productId, skuId);
        return ResponseEntity.noContent().build();
    }
}