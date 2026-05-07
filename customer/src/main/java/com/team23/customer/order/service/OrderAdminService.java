package com.team23.customer.order.service;

import com.team23.customer.order.domain.OrderStatus;
import com.team23.customer.order.dto.OrderAdminListResponse;
import com.team23.customer.order.repository.OrderAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderAdminService {
    private final OrderAdminRepository orderAdminRepository;
    
    @Transactional(readOnly = true)
    public Page<OrderAdminListResponse> search(
            OrderStatus status,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        LocalDateTime fromDt = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime toDt   = (to != null) ? to.atTime(23, 59, 59) : null;

        return orderAdminRepository
                .search(status, fromDt, toDt, pageable)
                .map(OrderAdminListResponse::from);
    }

}
