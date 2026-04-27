package com.team23.customer.order.domain;

import com.team23.customer.product.domain.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_lines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;  // 스냅샷

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency"))
    })
    private Money unitPrice;  // 스냅샷

    @Column(nullable = false)
    private int quantity;

    public static OrderLine of(Long productId, String productName, Money unitPrice, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        OrderLine line = new OrderLine();
        line.productId = productId;
        line.productName = productName;
        line.unitPrice = unitPrice;
        line.quantity = quantity;
        return line;
    }

    void attachToOrder(Order order) {
        this.order = order;
    }

    public Money getSubtotal() {
        return unitPrice.multiply(quantity);
    }
}