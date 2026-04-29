package com.team23.customer.product.controller;

import com.team23.customer.product.domain.Product;
import com.team23.customer.product.dto.ProductCreateRequest;
import com.team23.customer.product.dto.ProductDetailResponse;
import com.team23.customer.product.dto.ProductStockUpdateRequest;
import com.team23.customer.product.dto.ProductUpdateRequest;
import com.team23.customer.product.service.ProductAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.status(201)  // Created
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

    /**
     * 재고 증가 (입고).
     */
    @PostMapping("/{id}/restock")
    public ResponseEntity<ProductDetailResponse> restock(
            @PathVariable Long id,
            @Valid @RequestBody ProductStockUpdateRequest request
    ) {
        Product product = productAdminService.increaseStock(id, request.quantity());
        return ResponseEntity.ok(ProductDetailResponse.from(product));
    }

    /**
     * 상품 단종 처리 (Soft Delete).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> discontinue(@PathVariable Long id) {
        productAdminService.discontinue(id);
        return ResponseEntity.noContent().build();
    }
}