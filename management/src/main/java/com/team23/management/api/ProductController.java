package com.team23.management.api;

import com.team23.management.api.dto.*;
import com.team23.management.application.service.ProductService;
import com.team23.management.domain.product.Category;
import com.team23.management.domain.product.Product;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/seller/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductRegisterResponse> register(
            @Valid @RequestBody ProductRegisterRequest request
    ) {
        Long productId = productService.register(request.toCommand());

        ProductRegisterResponse response = new ProductRegisterResponse(
                productId,
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProductListResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Category category,
            Pageable pageable
    ) {
        Page<Product> products = productService.search(name, category, pageable);
        Page<ProductListResponse> response = products.map(ProductListResponse::from);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductUpdateResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        Product updated = productService.update(request.toCommand(id));
        return ResponseEntity.ok(ProductUpdateResponse.from(updated));
    }
}
