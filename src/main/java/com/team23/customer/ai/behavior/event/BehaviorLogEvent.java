package com.team23.customer.ai.behavior.event;

import com.team23.customer.ai.behavior.domain.ActionType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BehaviorLogEvent extends ApplicationEvent {

    private final Long memberId;
    private final String sessionId;
    private final ActionType actionType;
    private final Long productId;
    private final String keyword;
    private final String metadataJson;

    private BehaviorLogEvent(Object source, Long memberId, String sessionId, ActionType actionType, Long productId, String keyword, String metadataJson) {
        super(source);
        this.memberId = memberId;
        this.sessionId = sessionId;
        this.actionType = actionType;
        this.productId = productId;
        this.keyword = keyword;
        this.metadataJson = metadataJson;
    }

    // 상품 조회 이벤트용 정적 팩토리 메서드
    public static BehaviorLogEvent productView(Object source, Long memberId, String sessionId, Long productId) {
        return new BehaviorLogEvent(source, memberId, sessionId, ActionType.PRODUCT_VIEW, productId, null, null);
    }

    // 검색 이벤트용 정적 팩토리 메서드
    public static BehaviorLogEvent search(Object source, Long memberId, String sessionId, String keyword) {
        return new BehaviorLogEvent(source, memberId, sessionId, ActionType.SEARCH, null, keyword, null);
    }

    // 장바구니 추가 이벤트용 정적 팩토리 메서드
    public static BehaviorLogEvent addToCart(Object source, Long memberId, String sessionId, Long productId, int quantity, Long skuId) {
        // 수량, skuId 등 추가 정보는 JSON 형태로 metadata에 저장
        String metadata = String.format("{\"quantity\": %d, \"skuId\": %d}", quantity, skuId);
        return new BehaviorLogEvent(source, memberId, sessionId, ActionType.CART_ADD, productId, null, metadata);
    }

    // 주문 생성 이벤트용 정적 팩토리 메서드
    public static BehaviorLogEvent createOrder(Object source, Long memberId, String sessionId, String orderId) {
        // 주문 ID는 metadata에 저장
        String metadata = String.format("{\"orderId\": \"%s\"}", orderId);
        return new BehaviorLogEvent(source, memberId, sessionId, ActionType.ORDER_CREATE, null, null, metadata);
    }
}
