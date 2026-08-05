package com.shop.admin.product.controller;

import com.shop.admin.product.dto.SkuAdminAddRequest;
import com.shop.admin.product.dto.ProductAdminCreateRequest;
import com.shop.admin.product.dto.ProductAdminUpdateRequest;
import com.shop.admin.product.service.ProductAdminService;
import com.shop.global.response.CommonResponse;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.domain.SkuOption;
import com.shop.product.dto.ProductDetailResponse;
import com.shop.admin.product.dto.SkuAdminResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "어드민 - 상품", description = "상품 및 SKU 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {
    private final ProductAdminService productAdminService;

    @Operation(summary = "상품 등록", description = "새 상품을 등록한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류"),
            @ApiResponse(responseCode = "404", description = "카테고리 없음")
    })
    @PostMapping
    public ResponseEntity<CommonResponse<ProductDetailResponse>> register(
            @Valid @RequestBody ProductAdminCreateRequest request
    ) {
        Product product = productAdminService.register(request);
        return ResponseEntity.status(201)
                .body(CommonResponse.createSuccess(ProductDetailResponse.from(product)));
    }

    @Operation(summary = "상품 정보 수정", description = "상품 정보를 부분 수정한다 (PATCH 의미).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류"),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<ProductDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductAdminUpdateRequest request
    ) {
        // 1. 서비스로부터 DTO를 직접 받습니다.
        ProductDetailResponse responseDto = productAdminService.update(id, request);
        return ResponseEntity.ok(
                CommonResponse.createSuccess(responseDto) // 2. 받은 DTO를 그대로 응답으로 보냅니다.
        );
    }

    @Operation(summary = "상품 단종 처리", description = "상품을 단종 처리한다 (Soft Delete).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "단종 성공"),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> discontinue(@PathVariable Long id) {
        productAdminService.discontinue(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "SKU 추가", description = "상품에 새 SKU(옵션 조합)를 추가한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "추가 성공"),
            @ApiResponse(responseCode = "400", description = "중복 옵션 조합 / 단종 상품"),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    @PostMapping("/{productId}/skus")
    public ResponseEntity<CommonResponse<SkuAdminResponse>> addSku(
            @PathVariable Long productId,
            @Valid @RequestBody SkuAdminAddRequest request
    ) {
        List<SkuOption> options = request.options().stream()
                .map(opt -> new SkuOption(opt.name(), opt.value()))
                .toList();

        Sku sku = productAdminService.addSku(productId, options, request.initialStock());
        return ResponseEntity.status(201)
                .body(CommonResponse.createSuccess(SkuAdminResponse.from(sku)));
    }

    @Operation(summary = "SKU 제거", description = "재고가 0인 SKU만 제거 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "제거 성공"),
            @ApiResponse(responseCode = "400", description = "재고 있는 SKU"),
            @ApiResponse(responseCode = "404", description = "상품 또는 SKU 없음")
    })
    @DeleteMapping("/{productId}/skus/{skuId}")
    public ResponseEntity<Void> removeSku(
            @PathVariable Long productId,
            @PathVariable Long skuId
    ) {
        productAdminService.removeSku(productId, skuId);
        return ResponseEntity.noContent().build();
    }
}
