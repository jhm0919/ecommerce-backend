package com.team23.management.seller.dto;

public record RejectApplicationRequest(
        String reason   // 선택. 일단 받기만 (저장은 별도 이슈)
) {
}
