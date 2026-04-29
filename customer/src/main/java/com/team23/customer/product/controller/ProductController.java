package com.team23.customer.product.controller;

import com.team23.customer.product.domain.Product;
import com.team23.customer.product.dto.ProductDetailResponse;
import com.team23.customer.product.dto.ProductSummaryResponse;
import com.team23.customer.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 목록 조회.
     *
     * <p>페이지네이션 + 카테고리 필터 지원.
     *
     * <p>예시:
     * <ul>
     *   <li>GET /api/products?page=0&size=20</li>
     *   <li>GET /api/products?categoryId=1&page=0&size=20</li>
     *   <li>GET /api/products?page=0&size=20&sort=createdAt,desc</li>
     * </ul>
     */
    @GetMapping
    public ResponseEntity<Page<ProductSummaryResponse>> list(
            @RequestParam(required = false) Long categoryId,
            Pageable pageable
    ) {
        Page<Product> products = productService.findVisibleProducts(categoryId, pageable);
        Page<ProductSummaryResponse> response = products.map(ProductSummaryResponse::from);
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 상세 조회.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> getDetail(@PathVariable Long id) {
        Product product = productService.findById(id);
        return ResponseEntity.ok(ProductDetailResponse.from(product));
    }
}