package com.shop.admin.stock.service;

import com.shop.admin.stock.domain.StockType;
import com.shop.admin.stock.domain.StockHistory;
import com.shop.admin.stock.repository.StockHistoryRepository;
import com.shop.category.domain.Category;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.domain.SkuOption;
import com.shop.product.domain.StockChangedEvent;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock private StockHistoryRepository stockHistoryRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private StockService stockService;

    private StockHistory orderHistory;
    private StockHistory adminHistory;

    @BeforeEach
    void setUp() {
        orderHistory = StockHistory.of(
                1L, "티셔츠", 100L, "SKU-1-001", "색상=검정",
                StockType.ORDER, 3, 50, 47, 1001L
        );
        adminHistory = StockHistory.of(
                1L, "티셔츠", 100L, "SKU-1-001", "색상=검정",
                StockType.ADMIN_INCREASE, 10, 47, 57, null
        );
    }

    private Category createCategory() {
        return Category.create("남성 상의", "men-tops");
    }

    private Product createProduct() {
        return Product.register(
                "베이직 티셔츠",
                BigDecimal.valueOf(29900),
                "100% 면 소재",
                "https://example.com/image.jpg",
                createCategory()
        );
    }

    // ─── 테스트 헬퍼 ───

    private static void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("SKU 재고 증가 (increaseStock)")
    class IncreaseSkuStock {

        @Test
        @DisplayName("SKU 재고를 증가시킨다")
        void increaseSkuStockNormal() {
            Product product = createProduct();
            setId(product, 1L);
            Sku sku = product.addSku(List.of(new SkuOption("색상", "검정")), 10);
            setId(sku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            stockService.increaseStock(1L, 100L, 5);

            assertThat(sku.getStock()).isEqualTo(15);
        }

        @Test
        @DisplayName("재고 증가 시 StockChangedEvent 발행")  // ★ 추가
        void publishStockChangedEventOnIncrease() {
            Product product = createProduct();
            setId(product, 1L);
            Sku sku = product.addSku(List.of(new SkuOption("색상", "검정")), 10);
            setId(sku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            stockService.increaseStock(1L, 100L, 5);

            verify(eventPublisher).publishEvent(any(StockChangedEvent.class));
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID면 예외")
        void rejectUnknownProduct() {
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> stockService.increaseStock(999L, 100L, 5))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("SKU 재고 감소 (decreaseStock)")
    class DecreaseSkuStock {

        @Test
        @DisplayName("SKU 재고를 감소시킨다")
        void decreaseStockNormal() {
            Product product = createProduct();
            setId(product, 1L);
            Sku sku = product.addSku(List.of(new SkuOption("색상", "검정")), 10);
            setId(sku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            stockService.decreaseStock(1L, 100L, 3);

            assertThat(sku.getStock()).isEqualTo(7);
        }

        @Test
        @DisplayName("재고 감소 시 항상 StockChangedEvent 발행")  // ★ 추가
        void alwaysPublishStockChangedEvent() {
            Product product = createProduct();
            setId(product, 1L);
            Sku sku = product.addSku(List.of(new SkuOption("색상", "검정")), 10);
            setId(sku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            stockService.decreaseStock(1L, 100L, 3);  // 재고 10→7

            verify(eventPublisher).publishEvent(any(StockChangedEvent.class));
        }

        @Test
        @DisplayName("재고 0이어도 StockChangedEvent만 발행")
        void publishOnlyStockChangedEventWhenSoldOut() {
            Product product = createProduct();
            setId(product, 1L);
            Sku sku = product.addSku(List.of(new SkuOption("색상", "검정")), 3);
            setId(sku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            stockService.decreaseStock(1L, 100L, 3);  // 재고 3→0

            verify(eventPublisher).publishEvent(any(StockChangedEvent.class));
        }

        @Test
        @DisplayName("재고 남아도 StockChangedEvent는 발행")
        void publishStockChangedEventWhenStockRemains() {
            Product product = createProduct();
            setId(product, 1L);
            Sku sku = product.addSku(List.of(new SkuOption("색상", "검정")), 10);
            setId(sku, 100L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            stockService.decreaseStock(1L, 100L, 3);  // 재고 10→7

            verify(eventPublisher).publishEvent(any(StockChangedEvent.class));
        }
    }

    @Nested
    @DisplayName("재고 변동 이력 조회 (findHistories)")
    class FindHistories {

        @Test
        @DisplayName("Product 단위 전체 이력 조회")
        void findAllByProduct() {
            Pageable pageable = PageRequest.of(0, 20);
            given(stockHistoryRepository.findHistories(
                    eq(1L), eq(null), eq(null), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of(orderHistory, adminHistory)));

            Page<StockHistory> result = stockService.findHistories(
                    1L, null, null, pageable);

            assertThat(result.getContent()).hasSize(2);
            verify(stockHistoryRepository).findHistories(1L, null, null, pageable);
        }

        @Test
        @DisplayName("SKU 필터 적용")
        void filterBySkuId() {
            Pageable pageable = PageRequest.of(0, 20);
            given(stockHistoryRepository.findHistories(
                    eq(1L), eq(100L), eq(null), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of(orderHistory)));

            Page<StockHistory> result = stockService.findHistories(
                    1L, 100L, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(stockHistoryRepository).findHistories(1L, 100L, null, pageable);
        }

        @Test
        @DisplayName("changeType 필터 적용")
        void filterByChangeType() {
            Pageable pageable = PageRequest.of(0, 20);
            given(stockHistoryRepository.findHistories(
                    eq(1L), eq(null), eq(StockType.ORDER), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of(orderHistory)));

            Page<StockHistory> result = stockService.findHistories(
                    1L, null, StockType.ORDER, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getChangeType())
                    .isEqualTo(StockType.ORDER);
        }

        @Test
        @DisplayName("SKU + changeType 조합 필터")
        void filterBySkuAndChangeType() {
            Pageable pageable = PageRequest.of(0, 20);
            given(stockHistoryRepository.findHistories(
                    eq(1L), eq(100L), eq(StockType.ORDER), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of(orderHistory)));

            Page<StockHistory> result = stockService.findHistories(
                    1L, 100L, StockType.ORDER, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("이력이 없으면 빈 페이지 반환")
        void emptyResult() {
            Pageable pageable = PageRequest.of(0, 20);
            given(stockHistoryRepository.findHistories(
                    eq(1L), eq(null), eq(null), eq(pageable)))
                    .willReturn(Page.empty());

            Page<StockHistory> result = stockService.findHistories(
                    1L, null, null, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }
}