package com.shop.admin.stock.service;

import com.shop.product.domain.StockChangedEvent;
import com.shop.admin.stock.domain.StockHistory;
import com.shop.admin.stock.domain.StockHistoryRecordedEvent;
import com.shop.admin.stock.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockHistoryEventHandler {

    private final StockHistoryRepository stockHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRED)
    public void handleStockChanged(StockChangedEvent event) {
        StockHistory history = StockHistory.of(
                event.productId(),
                event.productName(),
                event.skuId(),
                event.skuCode(),
                event.skuOptionsSnapshot(),
                event.changeType(),
                event.quantity(),
                event.stockBefore(),
                event.stockAfter(),
                event.orderId()
        );
        StockHistory saved = stockHistoryRepository.save(history);
        eventPublisher.publishEvent(new StockHistoryRecordedEvent(saved));

        log.info("Stock history recorded: skuCode={}, type={}, qty={}, {}→{}",
                event.skuCode(), event.changeType(),
                event.quantity(), event.stockBefore(), event.stockAfter());
    }
}
