package com.team23.management.domain.Stock;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    private static final int MIN_QUANTITY = 0;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long skuId;  // ID 참조

    @Column(nullable = false)
    private int quantity;

    public static Stock create(Long skuId, int quantity) {
        validateQuantity(quantity);

        Stock stock = new Stock();
        stock.skuId = skuId;
        stock.quantity = quantity;
        return stock;
    }

    private static void validateQuantity(int quantity) {
        if (quantity < MIN_QUANTITY) {
            throw new IllegalArgumentException("수량은 0이상 이어야 합니다.");
        }
    }
}
