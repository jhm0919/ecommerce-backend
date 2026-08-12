package com.shop.order;

import com.shop.category.domain.Category;
import com.shop.category.repository.CategoryRepository;
import com.shop.order.dto.CreateOrderRequest;
import com.shop.order.repository.OrderRepository;
import com.shop.order.service.OrderService;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.domain.SkuOption;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test-mysql")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderConcurrencyTest {

    @Autowired OrderService orderService;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired OrderRepository orderRepository;

    Long productId;
    Long skuId;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        orderRepository.deleteAll();

        Category savedCategory = categoryRepository.saveAndFlush(Category.create("의류", "clothing"));

        Product savedProduct = productRepository.saveAndFlush(
                Product.register("티셔츠", 29900, "설명", "img", savedCategory)
        );

        savedProduct.addSku(List.of(new SkuOption("색상", "검정")), 100);
        savedProduct = productRepository.saveAndFlush(savedProduct);

        productId = savedProduct.getId();
        skuId = savedProduct.getSkuses().get(0).getId();

        System.out.println("=============초기 데이터 생성=============");
    }

    private CreateOrderRequest createRequest() {
        return new CreateOrderRequest(
                List.of(new CreateOrderRequest.OrderItemRequest(productId, skuId, 1)),
                "12345", "010-1234-5678",
                "홍길동", "01012345789", "문 앞에"
        );


    }

    @Test
    public void 동시에_100개_주문() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderService.createOrder(1L, createRequest());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Product product = productRepository.findByIdWithSkus(productId).orElseThrow(() -> new ProductNotFoundException(productId));

        List<Sku> skus = product.getSkus();

        int quantity = 0;

        for (Sku sku : skus) {
            quantity += sku.getQuantity();
        }

        assertThat(quantity).isEqualTo(0);
    }

    @Test
    void 한개_주문() {
        orderService.createOrder(1L, createRequest());
        System.out.println("=============주문=============");


        Product product = productRepository.findByIdWithSkus(productId).orElseThrow(() -> new ProductNotFoundException(productId));

        List<Sku> skus = product.getSkus();

        int quantity = 0;

        for (Sku sku : skus) {
            quantity += sku.getQuantity();
        }

        assertThat(quantity).isEqualTo(99);
    }

}
