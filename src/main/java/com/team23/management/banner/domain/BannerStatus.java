package com.team23.management.banner.domain;

/**
 * 배너 게시 상태.
 *
 * <p>DB에 저장되는 값: DRAFT, PUBLISHED 두 가지.
 * <p>응답 시 시간 기반으로 SCHEDULED / EXPIRED 가 동적 계산되어 표시됨.
 *
 * <ul>
 *   <li>{@link #DRAFT} — 등록만 됨 (publish 호출 전)</li>
 *   <li>{@link #SCHEDULED} — publish 됐고 startAt 미도래 (응답용)</li>
 *   <li>{@link #PUBLISHED} — publish 됐고 게시 중 (startAt <= now < endAt)</li>
 *   <li>{@link #EXPIRED} — publish 됐고 endAt 도래 (응답용)</li>
 * </ul>
 */
public enum BannerStatus {
    DRAFT,
    SCHEDULED,
    PUBLISHED,
    EXPIRED
}
