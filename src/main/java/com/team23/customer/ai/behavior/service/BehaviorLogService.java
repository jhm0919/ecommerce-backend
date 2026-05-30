package com.team23.customer.ai.behavior.service;

import com.team23.customer.ai.behavior.domain.UserBehaviorLog;
import com.team23.customer.ai.behavior.event.BehaviorLogEvent;
import com.team23.customer.ai.behavior.repository.UserBehaviorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BehaviorLogService {
    private final UserBehaviorLogRepository userBehaviorLogRepository;

    /**
     * BehaviorLogEvent를 비동기적으로 수신하여 DB에 저장합니다.
     */
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBehaviorLogEvent(BehaviorLogEvent event) {
        try {
            UserBehaviorLog userBehaviorLog = UserBehaviorLog.of(
                    event.getMemberId(),
                    event.getSessionId(),
                    event.getActionType(),
                    event.getProductId(),
                    event.getKeyword(),
                    event.getMetadataJson()
            );
            userBehaviorLogRepository.save(userBehaviorLog);
            log.info("[Behavior Log] 행동 로그 저장 완료: memberId={}, action={}", event.getMemberId(), event.getActionType());
        } catch (Exception e) {
            log.error("[Behavior Log] 행동 로그 저장 실패: {}", e.getMessage(), e);
        }
    }
}
