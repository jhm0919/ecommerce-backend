package com.shop.auth;

import com.shop.order.delivery.domain.Address;
import com.shop.order.delivery.domain.Receiver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VO 통합 시나리오")
class ValueObjectIntegrationTest {

    @Test
    @DisplayName("배송 정보를 표현할 수 있다 (Address + Receiver)")
    void representDeliveryInfo() {
        Address address = new Address(
                "06236",
                "서울시 강남구 테헤란로 152",
                "강남파이낸스센터 10층"
        );
        Receiver receiver = new Receiver("홍길동", "010-1234-5678");

        // 화면에 표시할 정보
        String displayInfo = String.format(
                "[%s] %s, %s",
                receiver.getMaskedName(),
                receiver.getMaskedPhone(),
                address.getFullAddress()
        );

        assertThat(displayInfo).isEqualTo(
                "[홍*동] 010-****-5678, 06236 서울시 강남구 테헤란로 152 강남파이낸스센터 10층"
        );
    }

//    @Test
//    @DisplayName("주문 금액 계산 시나리오 (Money 활용)")
//    void calculateOrderTotal() {
//        Money product1Price = Money.construct(15000);
//        Money product2Price = Money.construct(8000);
//        Money shippingFee = Money.construct(3000);
//
//        // 상품 합계 (수량 고려)
//        Money product1Subtotal = product1Price.multiply(2);  // 30000
//        Money product2Subtotal = product2Price.multiply(1);  // 8000
//        Money productTotal = product1Subtotal.add(product2Subtotal);  // 38000
//
//        // 최종 금액
//        Money finalAmount = productTotal.add(shippingFee);  // 41000
//
//        assertThat(finalAmount).isEqualTo(Money.construct(41000));
//    }
}
