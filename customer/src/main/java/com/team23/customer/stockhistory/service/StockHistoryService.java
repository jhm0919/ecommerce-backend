package com.team23.customer.stockhistory.service;

import com.team23.customer.stockhistory.domain.StockChangeType;
import com.team23.customer.stockhistory.domain.StockHistory;
import com.team23.customer.stockhistory.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockHistoryService {

    private final StockHistoryRepository stockHistoryRepository;

    @Transactional(readOnly = true)
    public Page<StockHistory> findHistories(
            Long productId,
            Long skuId,
            StockChangeType changeType,
            Pageable pageable
    ) {
        return stockHistoryRepository.findHistories(
                productId, skuId, changeType, pageable
        );
    }
}
