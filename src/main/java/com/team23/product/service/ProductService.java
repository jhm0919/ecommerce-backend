package com.team23.product.service;

import com.team23.ai.behavior.event.BehaviorLogEvent;
import com.team23.product.domain.Product;
import com.team23.product.domain.ProductStatus;
import com.team23.product.exception.ProductNotFoundException;
import com.team23.product.dto.response.ProductListResponse;
import com.team23.product.repository.ProductRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher; // 이벤트 발행기 주입
    private static final List<ProductStatus> CUSTOMER_VISIBLE = List.of(
            ProductStatus.ACTIVE,
            ProductStatus.SOLD_OUT
    );
    private static final String SORT_CREATED_AT = "createdAt";

    @Timed(
            value = "product.search.time",
            description = "상품 목록 조회 처리 시간"
    )
    @Transactional(readOnly = true)
    public Page<ProductListResponse> findProductList(
            Long categoryId, String keyword, Pageable pageable, Long memberId, String sessionId
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);
        Pageable sanitizedPageable = sanitizePageable(pageable);

        // 검색 행동 로그 이벤트 발행 (검색어가 비어있지 않은 경우)
        if (keyword != null && !keyword.isBlank()) {
            eventPublisher.publishEvent(BehaviorLogEvent.search(this, memberId, sessionId, keyword));
        }

        return productRepository.findProductList(
                categoryId,
                normalizedKeyword,
                ProductStatus.DISCONTINUED,
                sanitizedPageable
        ).map(ProductListResponse::from);
    }
    /**
     * 상품 상세 조회.
     * DISCONTINUED 상품은 사용자에게 노출하지 않으므로 NotFound로 응답.
     */
    @Timed(
            value = "product.detail.time",
            description = "상품 상세 조회 처리 시간"
    )
    @Transactional(readOnly = true)
    public Product findById(Long id, Long memberId, String sessionId) {
        Product product = productRepository.findByIdWithCategory(id).orElseThrow(() -> new ProductNotFoundException(id));

        // DISCONTINUED 상품은 사용자에게 "없는 것처럼" 응답
        if (!product.isVisibleToCustomer()) {
            throw new ProductNotFoundException(id);
        }

        // 행동 로그 이벤트 발행
        eventPublisher.publishEvent(BehaviorLogEvent.productView(this, memberId, sessionId, product.getId()));

        return product;
    }

    /**
     * 검색어 정제.
     * - 공백 제거
     * - 빈 문자열 → null (검색 안 함)
     */
    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Pageable sanitizePageable(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        for (Sort.Order order : pageable.getSort()) {
            if (!SORT_CREATED_AT.equals(order.getProperty())) {
                throw new IllegalArgumentException(
                        "Unsupported product sort field: " + order.getProperty());
            }
        }

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );
    }
}
