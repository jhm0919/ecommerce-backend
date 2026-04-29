package com.team23.management.domain.Sku;

import com.team23.management.domain.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "skus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sku {

    private static final int MIN_PRICE = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id")
    private List<SkuOptionValue> options = new ArrayList<>(); // NPE 방지. options.add(...) 하기 전에 List 가 반드시 존재해야.

    @Column(nullable = false)
    private int additionalPrice;

    public static Sku create(Long productId, List<SkuOptionInput> inputs, int additionalPrice) {
        Sku sku = new Sku();
        sku.productId = productId;
        sku.additionalPrice = additionalPrice;

        validateOptionSize(inputs);
        validateOptionDuplicate(inputs);
        validatePrice(additionalPrice);

        // 변환: SkuOptionInput → SkuOptionValue
        sku.options = new ArrayList<>();
        for (SkuOptionInput input : inputs) {
            SkuOptionValue value = new SkuOptionValue(input.name(), input.value());
            sku.options.add(value);
        }

        return sku;
    }

    private static void validatePrice(int additionalPrice) {
        if (additionalPrice < MIN_PRICE) {
            throw new IllegalArgumentException("가격은 0이하일 수 없습니다.");
        }
    }

    private static void validateOptionDuplicate(List<SkuOptionInput> inputs) {
        List<String> names = inputs.stream()
                .map(SkuOptionInput::name)
                .toList();

        Set<String> uniqueNames = new HashSet<>(names);

        if (names.size() != uniqueNames.size()) {
            throw new IllegalArgumentException("동일 옵션명이 중복될 수 없습니다.");
        }
    }

    private static void validateOptionSize(List<SkuOptionInput> inputs) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("옵션을 최소 1개 이상 선택해주세요.");
        }
    }


}
