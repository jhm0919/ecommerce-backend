package com.team23.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderNumber {

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String value;

    private OrderNumber(String value) {
        this.value = value;
    }

    public static OrderNumber generate() {
        // 형식: ORD-20260424-XXXXXX
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return new OrderNumber("ORD-" + date + "-" + random);
    }
}