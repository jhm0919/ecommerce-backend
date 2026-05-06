package com.team23.customer.notification.controller;

import com.team23.customer.notification.dto.NotificationResponse;
import com.team23.customer.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 전체 알림 목록.
     * GET /api/admin/notifications?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(
                notificationService.findAll(pageable)
                        .map(NotificationResponse::from)
        );
    }

    /**
     * 안읽은 알림 수.
     * GET /api/admin/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> countUnread() {
        return ResponseEntity.ok(
                Map.of("count", notificationService.countUnread())
        );
    }

    /**
     * 읽음 처리.
     * PATCH /api/admin/notifications/{id}/read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 전체 읽음 처리.
     * PATCH /api/admin/notifications/read-all
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}
