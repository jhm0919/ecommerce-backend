package com.shop.admin.order.service;

import com.shop.admin.order.dto.OrderAdminConfirmResponse;
import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;
import com.shop.order.domain.Order;
import com.shop.order.domain.OrderStatus;
import com.shop.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderAdminServiceTest {

    @InjectMocks
    OrderAdminService orderAdminService;
    @Mock
    OrderRepository orderRepository;

    @Test
    @DisplayName("PENDING 주문 일괄 확정 → CONFIRMED")
    void confirmPendingOrdersReturnsSuccessCount() {
        //given
        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);

        given(order1.getId()).willReturn(1L);
        given(order1.getStatus()).willReturn(OrderStatus.PENDING);

        given(order2.getId()).willReturn(2L);
        given(order2.getStatus()).willReturn(OrderStatus.PENDING);

        given(orderRepository.findById(1L)).willReturn(Optional.of(order1));
        given(orderRepository.findById(2L)).willReturn(Optional.of(order2));

        //when
        OrderAdminConfirmResponse response =
                orderAdminService.confirm(List.of(order1.getId(), order2.getId()));

        //then
        assertThat(response.successCount()).isEqualTo(2);
        assertThat(response.confirmedOrderIds())
                .containsExactlyInAnyOrder(order1.getId(), order2.getId());

        verify(order1).confirm();
        verify(order2).confirm();
        verify(orderRepository).findById(1L);
        verify(orderRepository).findById(2L);
    }

    @Test
    @DisplayName("단건 확정 → CONFIRMED")
    void confirmSingleOrderChangesStatusToConfirmed() {
        //given
        Order order1 = mock(Order.class);

        given(order1.getId()).willReturn(1L);
        given(order1.getStatus()).willReturn(OrderStatus.PENDING);

        given(orderRepository.findById(1L)).willReturn(Optional.of(order1));

        //when
        OrderAdminConfirmResponse response =
                orderAdminService.confirm(List.of(order1.getId()));

        //then
        assertThat(response.successCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("없는 orderId → 예외")
    void cancelNotFoundOrderThrowsException() {
        assertThatThrownBy(() ->
                orderAdminService.cancel(99999L, "사유")
        ).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("이미 취소된 주문 → 예외")
    void cancelAlreadyCancelledOrderThrowsException() {
        //given
        Order order1 = mock(Order.class);

        given(order1.getId()).willReturn(1L);
        given(order1.getStatus()).willReturn(OrderStatus.CANCELLED);

        given(orderRepository.findById(1L)).willReturn(Optional.of(order1));

        //when & then
        assertThatThrownBy(() ->
                orderAdminService.cancel(order1.getId(), "사유")
        ).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_ORDER_STATUS);
    }
}
