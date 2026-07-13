package com.team23.domain;

import com.team23.order.delivery.domain.Address;
import com.team23.order.delivery.domain.Delivery;
import com.team23.order.delivery.domain.DeliveryStatus;
import com.team23.order.delivery.domain.Receiver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DeliveryTest {

    private Address address;
    private Receiver receiver;

    @BeforeEach
    void setUp() {
        address = new Address("12345", "서울시 강남구 테헤란로 1", "101호");
        receiver = new Receiver("홍길동", "010-1234-5678");
    }

    @Test
    @DisplayName("배송을 PREPARING 상태로 생성한다")
    void prepareDelivery() {
        Delivery delivery = Delivery.prepare(
                1L, address, receiver, "문 앞에 두세요"
        );

        assertThat(delivery.getOrderId()).isEqualTo(1L);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PREPARING);
        assertThat(delivery.getAddress()).isEqualTo(address);
        assertThat(delivery.getReceiver()).isEqualTo(receiver);
        assertThat(delivery.getMemo()).isEqualTo("문 앞에 두세요");
    }

    @Nested
    @DisplayName("배송 시작 (startShipping)")
    class StartShipping {

        @Test
        @DisplayName("PREPARING 상태에서 배송 시작 가능")
        void startFromPreparing() {
            Delivery delivery = Delivery.prepare(1L, address, receiver, null);

            delivery.startShipping("CJ123456789");

            assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.IN_TRANSIT);
            assertThat(delivery.getTrackingNumber()).isEqualTo("CJ123456789");
            assertThat(delivery.getShippedAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 배송 중인 건 다시 시작 불가")
        void cannotStartTwice() {
            Delivery delivery = Delivery.prepare(1L, address, receiver, null);
            delivery.startShipping("CJ123");

            assertThatThrownBy(() -> delivery.startShipping("CJ456"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("배송 완료 (completeDelivery)")
    class CompleteDelivery {

        @Test
        @DisplayName("IN_TRANSIT 상태에서 완료 가능")
        void completeFromInTransit() {
            Delivery delivery = Delivery.prepare(1L, address, receiver, null);
            delivery.startShipping("CJ123");

            delivery.completeDelivery();

            assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
            assertThat(delivery.getDeliveredAt()).isNotNull();
        }

        @Test
        @DisplayName("PREPARING 상태에서 바로 완료 불가")
        void cannotCompleteFromPreparing() {
            Delivery delivery = Delivery.prepare(1L, address, receiver, null);

            assertThatThrownBy(delivery::completeDelivery)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("배송 실패 (markAsFailed)")
    class MarkAsFailed {

        @Test
        @DisplayName("어느 상태에서든 실패 처리 가능")
        void markAsFailedFromAnyState() {
            Delivery delivery = Delivery.prepare(1L, address, receiver, null);

            delivery.markAsFailed();

            assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        }
    }
}