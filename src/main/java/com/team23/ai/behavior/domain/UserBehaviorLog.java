package com.team23.ai.behavior.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_behavior_logs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBehaviorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private ActionType actionType;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "keyword", length = 200)
    private String keyword;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static UserBehaviorLog of(
            Long memberId, String sessionId, ActionType actionType,
            Long productId, String keyword, String metadataJson
    ) {
        UserBehaviorLog log = new UserBehaviorLog();
        log.memberId = memberId;
        log.sessionId = sessionId;
        log.actionType = actionType;
        log.productId = productId;
        log.keyword = keyword;
        log.metadataJson = metadataJson;
        return log;
    }


}
