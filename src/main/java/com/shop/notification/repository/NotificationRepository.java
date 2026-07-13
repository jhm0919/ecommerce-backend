package com.shop.notification.repository;

import com.shop.notification.domain.Notification;
import com.shop.notification.domain.NotificationSourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 전체 알림 목록 (최신순).
     */
    Page<Notification> findAllByOrderByOccurredAtDesc(Pageable pageable);

    /**
     * 안읽은 알림 수.
     */
    long countByIsReadFalse();

    /**
     * 안읽은 알림 목록.
     */
    Page<Notification> findByIsReadFalseOrderByOccurredAtDesc(Pageable pageable);

    boolean existsBySourceTypeAndSourceId(
            NotificationSourceType sourceType,
            Long sourceId
    );
}
