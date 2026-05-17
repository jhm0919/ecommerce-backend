package com.team23.customer.notification.controller;

import com.team23.global.response.CommonResponse;
import com.team23.customer.notification.dto.NotificationResponse;
import com.team23.customer.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "어드민 - 알림", description = "품절 알림 관리 API")
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "전체 알림 목록", description = "알림을 최신순으로 페이지네이션 조회한다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<CommonResponse<Page<NotificationResponse>>> findAll(
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(
                        notificationService.findAll(pageable)
                                .map(NotificationResponse::from)
                )
        );
    }

    @Operation(summary = "안읽은 알림 수", description = "읽지 않은 알림 수를 반환한다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/unread-count")
    public ResponseEntity<CommonResponse<Map<String, Long>>> countUnread() {
        return ResponseEntity.ok(
                CommonResponse.createSuccess(
                        Map.of("count", notificationService.countUnread())
                )
        );
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "읽음 처리 성공"),
            @ApiResponse(responseCode = "404", description = "알림 없음")
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "전체 알림 읽음 처리", description = "안읽은 알림을 모두 읽음 처리한다.")
    @ApiResponse(responseCode = "204", description = "전체 읽음 처리 성공")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}
