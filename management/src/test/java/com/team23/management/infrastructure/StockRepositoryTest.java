package com.team23.management.infrastructure;

import com.team23.management.domain.stock.Stock;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StockRepositoryTest {

    @Autowired
    StockRepository stockRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("Stock 생성 시 quantity가 저장된다")
    void saveThenFindById() {
        //given
        Stock stock = Stock.create(1L, 1);

        //when
        Stock saved = stockRepository.save(stock);
        em.flush();
        em.clear();

        Stock found = stockRepository.findById(saved.getId()).orElseThrow();

        //then
        assertThat(found.getQuantity()).isEqualTo(1);
    }
}