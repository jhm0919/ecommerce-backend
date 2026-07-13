package com.team23.product.domain;

import com.team23.category.domain.Category;
import com.team23.product.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ProductTest {

    private Category category;
    private BigDecimal price;

    @BeforeEach
    void setUp() {
        category = Category.create("남성 상의", "men-tops");
        price = BigDecimal.valueOf(10000);
    }

    private Product createProduct() {
        return Product.register(
                "베이직 티셔츠",
                price,
                "100% 면 소재",
                "https://example.com/image.jpg",
                category
        );
    }

    // ─────────────────────────────────────
    // 등록
    // ─────────────────────────────────────

    @Nested
    @DisplayName("등록 (register)")
    class Register {

        @Test
        @DisplayName("등록 시 ACTIVE 상태로 시작한다")
        void registerAlwaysActive() {
            Product product = createProduct();

            BigDecimal price = BigDecimal.valueOf(10000);

            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
            assertThat(product.getName()).isEqualTo("베이직 티셔츠");
            assertThat(product.getPrice()).isEqualTo(new Money(price));
        }

        @Test
        @DisplayName("name이 null이면 예외")
        void rejectNullName() {
            assertThatThrownBy(() -> Product.register(
                    null, price, "desc", "img", category))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("name이 blank면 예외")
        void rejectBlankName() {
            assertThatThrownBy(() -> Product.register(
                    "  ", price, "desc", "img", category))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("price가 null이면 예외")
        void rejectNullPrice() {
            assertThatThrownBy(() -> Product.register(
                    "이름", null, "desc", "img", category))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("category가 null이면 예외")
        void rejectNullCategory() {
            assertThatThrownBy(() -> Product.register(
                    "이름", price, "desc", "img", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("description은 null도 가능 (선택 사항)")
        void allowNullDescription() {
            Product product = Product.register(
                    "이름", price, null, "img", category);

            assertThat(product.getDescription()).isNull();
        }
    }

    // ─────────────────────────────────────
    // SKU 재고 관리
    // ─────────────────────────────────────

    @Nested
    @DisplayName("SKU 재고 감소 (decreaseSkuStock)")
    class DecreaseSkuStock {

        @Test
        @DisplayName("SKU 재고를 감소시킬 수 있다")
        void decreaseNormal() {
            Product product = createProduct();
            setId(product, 1L);
            Sku sku = product.addSku(List.of(new SkuOption("색상", "검정")), 10);
            setId(sku, 100L);

            product.decreaseSkuStock(100L, 3);

            assertThat(sku.getStock()).isEqualTo(7);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("모든 SKU 재고 0이면 SOLD_OUT으로 자동 전이")
        void autoSoldOutWhenAllEmpty() {
            Product product = createProduct();
            setId(product, 1L);
            Sku sku1 = product.addSku(List.of(new SkuOption("색상", "검정")), 5);
            Sku sku2 = product.addSku(List.of(new SkuOption("색상", "흰색")), 3);
            setId(sku1, 100L);
            setId(sku2, 200L);

            product.decreaseSkuStock(100L, 5);
            product.decreaseSkuStock(200L, 3);

            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }

        @Test
        @DisplayName("일부 SKU에 재고 있으면 ACTIVE 유지")
        void stayActiveIfAnyStockRemains() {
            Product product = createProduct();
            setId(product, 1L);
            Sku sku1 = product.addSku(List.of(new SkuOption("색상", "검정")), 5);
            Sku sku2 = product.addSku(List.of(new SkuOption("색상", "흰색")), 3);
            setId(sku1, 100L);
            setId(sku2, 200L);

            product.decreaseSkuStock(100L, 5);  // sku1만 0

            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("SKU 재고 증가 (increaseSkuStock)")
    class IncreaseSkuStock {

        @Test
        @DisplayName("SKU 재고를 증가시킬 수 있다")
        void increaseNormal() {
            Product product = createProduct();
            setId(product, 1L);
            Sku sku = product.addSku(List.of(new SkuOption("색상", "검정")), 5);
            setId(sku, 100L);

            product.increaseSkuStock(100L, 10);

            assertThat(sku.getStock()).isEqualTo(15);
        }

        @Test
        @DisplayName("SOLD_OUT 상태에서 재고 입고 시 ACTIVE로 전환")
        void soldOutToActive() {
            Product product = createProduct();
            setId(product, 1L);
            Sku sku = product.addSku(List.of(new SkuOption("색상", "검정")), 1);
            setId(sku, 100L);
            product.decreaseSkuStock(100L, 1);  // SOLD_OUT
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);

            product.increaseSkuStock(100L, 5);

            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("DISCONTINUED 상품은 재고 증가 불가")
        void cannotIncreaseDiscontinued() {
            Product product = createProduct();
            setId(product, 1L);
            product.addSku(List.of(new SkuOption("색상", "검정")), 5);
            product.discontinue();

            assertThatThrownBy(() -> product.increaseSkuStock(100L, 5))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ─────────────────────────────────────
    // 단종
    // ─────────────────────────────────────

    @Nested
    @DisplayName("단종 (discontinue)")
    class Discontinue {

        @Test
        @DisplayName("ACTIVE 상품을 단종 처리할 수 있다")
        void discontinueActive() {
            Product product = createProduct();

            product.discontinue();

            assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }

        @Test
        @DisplayName("SOLD_OUT 상품도 단종 처리 가능")
        void discontinueSoldOut() {
            Product product = createProduct();
            setId(product, 1L);
            Sku sku = product.addSku(List.of(new SkuOption("색상", "검정")), 1);
            setId(sku, 100L);
            product.decreaseSkuStock(100L, 1);  // SOLD_OUT
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);

            product.discontinue();

            assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }

        @Test
        @DisplayName("이미 DISCONTINUED인 상품은 다시 단종 불가")
        void cannotDiscontinueAlready() {
            Product product = createProduct();
            product.discontinue();

            assertThatThrownBy(product::discontinue)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ─────────────────────────────────────
    // 정보 수정
    // ─────────────────────────────────────

    @Nested
    @DisplayName("정보 수정 (updateInfo)")
    class UpdateInfo {

        @Test
        @DisplayName("name만 수정할 수 있다")
        void updateNameOnly() {
            Product product = createProduct();

            product.update("새 이름", null, null, null, null);

            assertThat(product.getName()).isEqualTo("새 이름");
            assertThat(product.getDescription()).isEqualTo("100% 면 소재");
            assertThat(product.getMainImageUrl()).isEqualTo("https://example.com/image.jpg");
        }

        @Test
        @DisplayName("description만 수정할 수 있다")
        void updateDescriptionOnly() {
            Product product = createProduct();

            product.update(null, "새 설명", null, null, null);

            assertThat(product.getName()).isEqualTo("베이직 티셔츠");
            assertThat(product.getDescription()).isEqualTo("새 설명");
        }
    }

    // ─────────────────────────────────────
    // 가격 변경
    // ─────────────────────────────────────

    @Nested
    @DisplayName("가격 변경 (changePrice)")
    class ChangePrice {

        @Test
        @DisplayName("가격을 변경할 수 있다")
        void changePriceNormal() {
            Product product = createProduct();
            BigDecimal newPrice = BigDecimal.valueOf(39900);

            product.update(null, null, null, newPrice, null);

            assertThat(product.getPrice()).isEqualTo(new Money(newPrice));
        }

        @Test
        @DisplayName("DISCONTINUED 상품은 가격 변경 불가")
        void cannotChangePriceOfDiscontinued() {
            Product product = createProduct();
            product.discontinue();

            BigDecimal newPrice = BigDecimal.valueOf(39900);

            assertThatThrownBy(() -> product.update(
                    null, null, null, newPrice, null
                    )).isInstanceOf(IllegalStateException.class);
        }
    }

    // ─────────────────────────────────────
    // 총 재고
    // ─────────────────────────────────────

    @Nested
    @DisplayName("총 재고 (getTotalSkuStock)")
    class TotalSkuStock {

        @Test
        @DisplayName("모든 SKU 재고의 합계를 반환한다")
        void returnsSum() {
            Product product = createProduct();
            setId(product, 1L);
            product.addSku(List.of(new SkuOption("색상", "검정")), 10);
            product.addSku(List.of(new SkuOption("색상", "흰색")), 20);
            product.addSku(List.of(new SkuOption("색상", "회색")), 30);

            assertThat(product.getTotalSkuStock()).isEqualTo(60);
        }

        @Test
        @DisplayName("SKU가 없으면 0")
        void zeroWhenNoSkus() {
            Product product = createProduct();

            assertThat(product.getTotalSkuStock()).isZero();
        }
    }

    // ─────────────────────────────────────
    // 테스트 헬퍼
    // ─────────────────────────────────────

    private static void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}