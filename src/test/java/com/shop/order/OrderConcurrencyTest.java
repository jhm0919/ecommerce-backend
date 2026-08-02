package com.shop.order;

import com.shop.category.domain.Category;
import com.shop.category.repository.CategoryRepository;
import com.shop.order.dto.CreateOrderRequest;
import com.shop.order.repository.OrderRepository;
import com.shop.order.service.OrderService;
import com.shop.product.domain.Product;
import com.shop.product.domain.SkuOption;
import com.shop.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
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
    @Autowired EntityManager entityManager;

    Long productId;
    Long skuId;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        orderRepository.deleteAll();

        Category savedCategory = categoryRepository.save(Category.create("의류", "clothing"));

        Product savedProduct = productRepository.saveAndFlush(
                Product.register("티셔츠", 29900, "설명", "img", savedCategory)
        );

        savedProduct.addSku(List.of(new SkuOption("색상", "검정")), 1);
        savedProduct = productRepository.saveAndFlush(savedProduct);

        productId = savedProduct.getId();
        skuId = savedProduct.getSkuses().get(0).getId();

        System.out.println("productId = " + productId);
        System.out.println("skuId = " + skuId);

        entityManager.clear();
    }

    private CreateOrderRequest createRequest() {
        return new CreateOrderRequest(
                List.of(new CreateOrderRequest.OrderItemRequest(productId, skuId, 1)),
                "12345", "010-1234-5678",
                "홍길동", "01012345789", "문 앞에"
        );
    }

    @Test
    void concurrentOrders_shouldExposeStockRace() throws Exception {
        // 2. 동시에 시작시키기 위한 latch

        // 생성할 작업 스레드가 사용할 객채(스레드 수 만큼 생성)
        CountDownLatch ready = new CountDownLatch(2);
        // 메인 스레드(테스트 코드) 가 사용할 객체로 1개만 생성
        CountDownLatch start = new CountDownLatch(1);

        // 3. 주문 2개를 동시에 실행
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Boolean> task = () -> {
            // 각 스레드가 준비 완료임을 알리는 데 사용된다.
            // 두 스레드가 ready.countDown()을 호출해야 ready.await() 이후 테스트 스레드가 start 신호를 보내줄 수 있다.
            ready.countDown();
            // 각 스레드가 실행 전에 대기하는 메서드.
            // 메인 스레드에서 start.countDown()이 호출될 때까지 각 스레드는 처리 로직(승인)을 실행하지 않고 대기한다.
            start.await();

            try {
                orderService.createOrder(1L, createRequest());
                return true;
            } catch (Exception e) {
                return false;
            }
        };

        Future<Boolean> f1 = executor.submit(task);
        Future<Boolean> f2 = executor.submit(task);

        // 메인 스레드가 두 스레드가 준비됐는지 확인하기 위해 사용.
        // 두 스레드가 모두 ready.countDown()을 호출해 카운터가 0이 되면 ready.await()에서 대기 중인 메인 스레드가 해제된다.
        ready.await();
        // 두 스레드가 동시에 출발할 수 있도록 start 래치의 카운터를 감소시켜 0으로 만든다.
        // 두 스레드는 start.await()에서 기다리고 있다가 start.countDown() 호출 후 동시에 실행을 시작한다.
        start.countDown();

        boolean result1 = f1.get();
        boolean result2 = f2.get();

        int successCount = 0;
        if (result1) successCount++;
        if (result2) successCount++;

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        entityManager.clear();

        System.out.println("successCount = " + successCount);
        System.out.println("orderCount = " + orderRepository.count());

        // 4. 재고/주문 수 검증
        assertThat(successCount).isEqualTo(1);
        assertThat(orderRepository.count()).isEqualTo(1L);
    }


    // todo: 주문 취소시 동시성 문제 테스트

}
