package com.team23.management.domain.Sku;

import lombok.Getter;

public class SkuOptionValue { // Sku 가 보관하는 옵션 정보 (Entity 후보)
    private Long id;
    @Getter
    private String name;
    @Getter
    private String value;

    public SkuOptionValue(String name, String value) {
        this.name = name;
        this.value = value;
    }

}
