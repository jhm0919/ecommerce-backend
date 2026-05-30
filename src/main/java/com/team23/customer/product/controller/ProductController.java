package com.team23.customer.product.controller;

import com.team23.common.response.CommonResponse;
import com.team23.common.security.jwt.AuthPrincipal;
import com.team23.customer.product.domain.Product;
import com.team23.customer.product.dto.ProductDetailResponse;
import com.team23.customer.product.dto.ProductSummaryResponse;
import com.team23.customer.product.service.ProductService;
import io.micrometer.core.annotation.Counted;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "상품", description = "상품 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "상품 목록 조회",
            description = "페이지네이션 + 카테고리 필터 + 키워드 검색 지원.\n\n" +
                    "예시:\n" +
                    "- GET /api/products?page=0&size=20\n" +
                    "- GET /api/products?categoryId=1\n" +
                    "- GET /api/products?keyword=티셔츠\n" +
                    "- GET /api/products?page=0&size=20&sort=createdAt,desc"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @Counted(
            value = "product.search",
            description = "상품 목록 조회 요청 수"
    )
    @GetMapping
    public ResponseEntity<CommonResponse<Page<ProductSummaryResponse>>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        Page<ProductSummaryResponse> products = productService.findVisibleProducts(
                categoryId, keyword, pageable
        );
        return ResponseEntity.ok(
                CommonResponse.createSuccess(products)
        );
    }

    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상세 정보를 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    @Counted(
            value = "product.detail",
            description = "상품 상세 조회 요청 수"
    )
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ProductDetailResponse>> getDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthPrincipal principal,
            @CookieValue(name = "session-id", required = false) String sessionId
    ) {
        // 로그인 사용자는 memberId, 비로그인 사용자는 null
        Long memberId = (principal != null) ? principal.memberId() : null;

        var product = productService.findById(id, memberId, sessionId);

        return ResponseEntity.ok(
                CommonResponse.createSuccess(ProductDetailResponse.from(product))
        );
    }
}
