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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sku {

    private static final int MIN_SIZE = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    private Long productId;

    @Getter
    private List<SkuOptionValue> options;

    @Getter
    private int additionalPrice;

    public static Sku create(Long productId, List<SkuOptionInput> inputs, int additionalPrice) {
        Sku sku = new Sku();
        sku.productId = productId;
        sku.additionalPrice = additionalPrice;

        validateOptionSize(inputs);
        validateOptionDuplicate(inputs);

        // 변환: SkuOptionInput → SkuOptionValue
        sku.options = new ArrayList<>();
        for (SkuOptionInput input : inputs) {
            SkuOptionValue value = new SkuOptionValue(input.name(), input.value());
            sku.options.add(value);
        }

        return sku;
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
