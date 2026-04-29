package com.team23.management.domain.Sku;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sku_option_values")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkuOptionValue { // Sku 가 보관하는 옵션 정보 (Entity 후보)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "option_value", nullable = false)
    private String value;

    SkuOptionValue(String name, String optionValue) { // 같은 패키지 내 Sku 만 호출 가능.
        this.name = name;
        this.value = optionValue;
    }

}
