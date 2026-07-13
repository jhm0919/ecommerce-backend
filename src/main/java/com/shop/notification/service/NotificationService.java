package com.shop.notification.service;

import com.shop.notification.domain.Notification;
import com.shop.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 전체 알림 목록 조회 (최신순).
     */
    @Transactional(readOnly = true)
    public Page<Notification> findAll(Pageable pageable) {
        return notificationRepository.findAllByOrderByOccurredAtDesc(pageable);
    }

    /**
     * 안읽은 알림 수.
     */
    @Transactional(readOnly = true)
    public long countUnread() {
        return notificationRepository.countByIsReadFalse();
    }

    /**
     * 읽음 처리.
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification not found: id=" + notificationId));

        notification.markAsRead();
        log.info("Notification marked as read: id={}", notificationId);
    }

    /**
     * 전체 읽음 처리.
     */
    @Transactional
    public void markAllAsRead() {
        notificationRepository.findByIsReadFalseOrderByOccurredAtDesc(Pageable.unpaged())
                .forEach(Notification::markAsRead);

        log.info("All notifications marked as read");
    }
}
