package com.shop.admin.order.controller;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;
import com.shop.global.security.jwt.JwtAuthenticationProvider;
import com.shop.order.domain.OrderStatus;
import com.shop.admin.order.dto.OrderAdminCancelRequest;
import com.shop.admin.order.dto.OrderAdminCancelResponse;
import com.shop.admin.order.dto.OrderAdminConfirmRequest;
import com.shop.admin.order.dto.OrderAdminConfirmResponse;
import com.shop.admin.order.dto.OrderAdminListResponse;
import com.shop.admin.order.service.OrderAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderAdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    OrderAdminService orderAdminService;
    @MockitoBean
    JwtAuthenticationProvider jwtAuthenticationProvider;

    @Test
    @DisplayName("GET /api/admin/orders - 전체 조회 200")
    void searchAllReturns200() throws Exception {
        when(orderAdminService.search(eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(pageOf(
                        orderResponse(1L, "ORD-001", OrderStatus.PENDING),
                        orderResponse(2L, "ORD-002", OrderStatus.PENDING)
                ));

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].orderNumber").value("ORD-001"))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data.content[0].itemCount").value(1));
    }

    @Test
    @DisplayName("상태 필터 - 200")
    void searchByStatusReturns200() throws Exception {
        when(orderAdminService.search(eq(OrderStatus.PENDING), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(pageOf(orderResponse(1L, "ORD-001", OrderStatus.PENDING)));

        mockMvc.perform(get("/api/admin/orders")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("기간 필터 - 200")
    void searchByDateRangeReturns200() throws Exception {
        LocalDate today = LocalDate.now();
        when(orderAdminService.search(eq(null), eq(today), eq(today), any(Pageable.class)))
                .thenReturn(pageOf(orderResponse(1L, "ORD-001", OrderStatus.PENDING)));

        mockMvc.perform(get("/api/admin/orders")
                        .param("from", today.toString())
                        .param("to", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("결과 없는 필터 - totalElements 0")
    void searchNoResultReturnsEmpty() throws Exception {
        when(orderAdminService.search(eq(OrderStatus.CONFIRMED), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/orders")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("PATCH /api/admin/orders/confirm - 정상 확정 200")
    void confirmOrdersReturns200() throws Exception {
        OrderAdminConfirmRequest request =
                new OrderAdminConfirmRequest(List.of(1L, 2L));
        when(orderAdminService.confirm(List.of(1L, 2L)))
                .thenReturn(new OrderAdminConfirmResponse(2, List.of(1L, 2L)));

        mockMvc.perform(patch("/api/admin/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.successCount").value(2))
                .andExpect(jsonPath("$.data.confirmedOrderIds").isArray());
    }

    @Test
    @DisplayName("없는 orderId 확정 요청 - 404")
    void confirmWithNotFoundIdReturns404() throws Exception {
        OrderAdminConfirmRequest request =
                new OrderAdminConfirmRequest(List.of(99999L));
        when(orderAdminService.confirm(List.of(99999L)))
                .thenThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND) {});

        mockMvc.perform(patch("/api/admin/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("CANCELLED 주문 확정 요청 - 400")
    void confirmCancelledOrderReturns400() throws Exception {
        OrderAdminConfirmRequest request =
                new OrderAdminConfirmRequest(List.of(1L));
        when(orderAdminService.confirm(List.of(1L)))
                .thenThrow(new BusinessException(ErrorCode.INVALID_ORDER_STATUS) {});

        mockMvc.perform(patch("/api/admin/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("빈 목록 확정 요청 - 400")
    void confirmEmptyListReturns400() throws Exception {
        OrderAdminConfirmRequest request =
                new OrderAdminConfirmRequest(List.of());

        mockMvc.perform(patch("/api/admin/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"));

        verify(orderAdminService, never()).confirm(any());
    }

    @Test
    @DisplayName("POST /api/admin/orders/{orderId}/cancel - 정상 취소 200")
    void cancelOrderReturns200() throws Exception {
        OrderAdminCancelRequest request =
                new OrderAdminCancelRequest("재고 부족", "OUT_OF_STOCK");
        when(orderAdminService.cancel(1L, "재고 부족", "OUT_OF_STOCK"))
                .thenReturn(new OrderAdminCancelResponse(
                        1L, "ORD-001", OrderStatus.CANCELLED, "재고 부족"));

        mockMvc.perform(post("/api/admin/orders/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.cancelReason").value("재고 부족"));
    }

    @Test
    @DisplayName("없는 orderId 취소 요청 - 404")
    void cancelNotFoundOrderReturns404() throws Exception {
        OrderAdminCancelRequest request =
                new OrderAdminCancelRequest("사유", "CODE");
        when(orderAdminService.cancel(99999L, "사유", "CODE"))
                .thenThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND) {});

        mockMvc.perform(post("/api/admin/orders/99999/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("취소 사유 누락 - 400")
    void cancelWithBlankReasonReturns400() throws Exception {
        OrderAdminCancelRequest request =
                new OrderAdminCancelRequest("", "CODE");

        mockMvc.perform(post("/api/admin/orders/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"));

        verify(orderAdminService, never()).cancel(any(), any(), any());
    }

    @Test
    @DisplayName("이미 취소된 주문 취소 요청 - 400")
    void cancelAlreadyCancelledOrderReturns400() throws Exception {
        OrderAdminCancelRequest request =
                new OrderAdminCancelRequest("사유", "CODE");
        when(orderAdminService.cancel(1L, "사유", "CODE"))
                .thenThrow(new BusinessException(ErrorCode.ALREADY_CANCELLED_ORDER) {});

        mockMvc.perform(post("/api/admin/orders/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    private Page<OrderAdminListResponse> pageOf(OrderAdminListResponse... responses) {
        return new PageImpl<>(List.of(responses), PageRequest.of(0, 20), responses.length);
    }

    private OrderAdminListResponse orderResponse(
            Long orderId,
            String orderNumber,
            OrderStatus status
    ) {
        return new OrderAdminListResponse(
                orderId,
                orderNumber,
                BigDecimal.valueOf(10000),
                status,
                1,
                LocalDateTime.now()
        );
    }
}
