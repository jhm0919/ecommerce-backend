package com.team23.customer.ai;

import com.team23.customer.ai.behavior.domain.ActionType;
import com.team23.customer.ai.behavior.repository.UserBehaviorLogRepository;
import com.team23.customer.ai.chat.dto.FastApiChatRequest;
import com.team23.customer.ai.chat.service.AiChatDataCollector;
import com.team23.customer.cart.service.CartService;
import com.team23.customer.category.domain.Category;
import com.team23.customer.order.dto.CreateOrderRequest;
import com.team23.customer.order.service.OrderService;
import com.team23.customer.product.domain.*;
import com.team23.customer.category.repository.CategoryRepository;
import com.team23.customer.product.repository.ProductRepository;
import com.team23.customer.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.yaml")
@SpringBootTest
//@Transactional
public class AiFeatureIntegrationTest {

    @Autowired
    private ProductService productService;
    @Autowired
    private CartService cartService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private AiChatDataCollector aiChatDataCollector;

    @Autowired
    private UserBehaviorLogRepository userBehaviorLogRepository;

    // 테스트에서 공통으로 사용할 변수
    private Long testProductId;
    private Long testSkuId;
    private final Long testMemberId = 1L;
    private String testSessionId;

    @BeforeEach
    void setUp() {
        // 테스트간 데이터 충돌을 막기 위해 관련 테이블 초기화
        userBehaviorLogRepository.deleteAllInBatch();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // --- 테스트용 상품 데이터 생성 ---
        Category testCategory = categoryRepository.save(Category.create("테스트 카테고리", "men-tops"));
        Product newProduct = Product.register(
                "베이직 티셔츠",
                BigDecimal.valueOf(29900),
                "100% 면 소재",
                "https://example.com/image.jpg",
                testCategory
        );
        // SKU 추가를 위해 Product를 먼저 저장하여 ID를 할당받음
        Product savedProduct = productRepository.save(newProduct);

        // SKU 생성 및 추가
        Sku sku = savedProduct.addSku(List.of(new SkuOption("사이즈", "L")), 100);
        Product productWithSku = productRepository.save(savedProduct);// SKU가 추가된 상태를 다시 저장

        // 3. 생성된 상품의 ID를 테스트 변수에 저장
        this.testProductId = productWithSku.getId();
        this.testSkuId = productWithSku.getSkuses().get(0).getId();
        this.testSessionId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("상품 조회 시 행동 로그가 쌓이고, AiChatDataCollector가 이를 정상적으로 수집한다")
    void productViewLogCollectionTest() {
        // given: 테스트 데이터는 @BeforeEach에서 준비됨

        // when: 1. 사용자가 상품을 조회함 (이벤트가 비동기적으로 발행됨)
        Product product = productService.findById(testProductId, testMemberId, testSessionId);

        // then: 1. 비동기 작업이 완료되어 DB에 로그가 1개 쌓일 때까지 최대 5초간 기다림
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            long logCount = userBehaviorLogRepository.count();
            assertThat(logCount).isEqualTo(1);
        });

        // when: 2. AI 채팅 데이터 수집기를 실행함
        FastApiChatRequest result = aiChatDataCollector.collect(testMemberId, testSessionId, "이 상품 어때?");

        // then: 2. 수집된 데이터 검증
        assertThat(result.behaviorLogs()).hasSize(1);
        FastApiChatRequest.BehaviorLogDto collectedLog = result.behaviorLogs().get(0);
        assertThat(collectedLog.actionType()).isEqualTo(ActionType.PRODUCT_VIEW.name());
        assertThat(collectedLog.productId()).isEqualTo(testProductId); // 하드코딩된 ID 대신 변수 사용
        assertThat(collectedLog.keyword()).isNull();
    }

    @Test
    @DisplayName("검색 시 행동 로그가 쌓이고, AiChatDataCollector가 이를 정상적으로 수집한다")
    void searchLogCollectionTest() {
        // given
        String keyword = "테스트";
        // Pageable 객체를 직접 생성합니다. (0페이지, 10개 사이즈)
        Pageable pageable = PageRequest.of(0, 10);

        // when
        productService.findProductList(null, keyword, pageable, testMemberId, testSessionId);

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(userBehaviorLogRepository.count()).isEqualTo(1));

        // when
        FastApiChatRequest result = aiChatDataCollector.collect(testMemberId, testSessionId, "다른거 찾아줘");

        // then
        assertThat(result.behaviorLogs()).hasSize(1);
        FastApiChatRequest.BehaviorLogDto collectedLog = result.behaviorLogs().get(0);
        assertThat(collectedLog.actionType()).isEqualTo(ActionType.SEARCH.name());
        assertThat(collectedLog.keyword()).isEqualTo(keyword);
        assertThat(collectedLog.productId()).isNull();
    }

    @Test
    @DisplayName("장바구니 추가 시 행동 로그가 쌓이고, AiChatDataCollector가 이를 정상적으로 수집한다")
    void addToCart_LogCollection_Test() {
        // given
        int quantity = 2;

        // when
        cartService.addItem(testMemberId, testSessionId, testProductId, testSkuId, quantity);

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(userBehaviorLogRepository.count()).isEqualTo(1));

        // when
        FastApiChatRequest result = aiChatDataCollector.collect(testMemberId, testSessionId, "장바구니에 담았어");

        // then
        assertThat(result.behaviorLogs()).hasSize(1);
        FastApiChatRequest.BehaviorLogDto collectedLog = result.behaviorLogs().get(0);
        assertThat(collectedLog.actionType()).isEqualTo(ActionType.CART_ADD.name());
        assertThat(collectedLog.productId()).isEqualTo(testProductId);
    }

    @Test
    @DisplayName("주문 생성 시 행동 로그가 쌓이고, AiChatDataCollector가 이를 정상적으로 수집한다")
    void createOrder_LogCollection_Test() {
        // given
//        CreateOrderRequest.OrderItemRequest orderItemRequest = new CreateOrderRequest.OrderItemRequest(testProductId, testSkuId, 1);
        CreateOrderRequest orderRequest = new CreateOrderRequest(
                List.of(new CreateOrderRequest.OrderItemRequest(testProductId, testSkuId, 1)),
                new CreateOrderRequest.DeliveryInfoRequest(
                        "홍길동", "010-1234-5678",
                        "12345", "서울시 강남구", "101호", "문 앞에"
                ),
                null, null
        );

        // when
        orderService.createMemberOrderDetail(testMemberId, testSessionId, orderRequest);

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(userBehaviorLogRepository.count()).isEqualTo(1));

        // when
        FastApiChatRequest result = aiChatDataCollector.collect(testMemberId, testSessionId, "주문 완료!");

        // then
        assertThat(result.behaviorLogs()).hasSize(1);
        FastApiChatRequest.BehaviorLogDto collectedLog = result.behaviorLogs().get(0);
        assertThat(collectedLog.actionType()).isEqualTo(ActionType.ORDER_CREATE.name());
        // 주문 생성 로그는 특정 상품 ID나 키워드와 직접 연결되지 않으므로 null 체크
        assertThat(collectedLog.productId()).isNull();
        assertThat(collectedLog.keyword()).isNull();
    }
}
