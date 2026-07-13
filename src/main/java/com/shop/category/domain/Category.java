package com.shop.category.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 상품 카테고리.
 *
 * <p>현재는 단일 레벨 (계층 없음). 필요 시 parent 필드 추가하여 계층 구조로 확장 가능.
 *
 * <p>{@code slug}는 URL 친화적 식별자 (예: "men-tops")로, 검색 엔진 최적화(SEO)에 활용된다.
 */
@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_slug", columnNames = "slug"),
        @UniqueConstraint(name = "uk_category_name", columnNames = "name")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_SLUG_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Column(nullable = false, length = MAX_SLUG_LENGTH)
    private String slug;

    // ─────────────────────────────────────
    // 정적 팩토리
    // ─────────────────────────────────────

    /**
     * 새 카테고리를 생성한다.
     *
     * @param name 화면에 표시될 이름 (예: "남성 상의")
     * @param slug URL 식별자 (예: "men-tops")
     */
    public static Category create(String name, String slug) {
        // 1. 정제
        String normalizedName = normalizeName(name);
        String normalizedSlug = normalizeSlug(slug);

        // 2. 검증
        validateName(normalizedName);
        validateSlug(normalizedSlug);

        // 3. 생성
        Category category = new Category();
        category.name = normalizedName;
        category.slug = normalizedSlug;
        return category;
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드
    // ─────────────────────────────────────

    public void rename(String newName) {
        validateName(newName);
        this.name = normalizeName(newName);
    }

    public void changeSlug(String newSlug) {
        validateSlug(newSlug);
        this.slug = normalizeSlug(newSlug);
    }

    // ─────────────────────────────────────
    // 정제
    // ─────────────────────────────────────

    private static String normalizeName(String name) {
        return name.trim();
    }

    private static String normalizeSlug(String slug) {
        return slug.trim().toLowerCase();

    }

    // ─────────────────────────────────────
    // 검증
    // ─────────────────────────────────────

    private static void validateName(String name) {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "name must not exceed " + MAX_NAME_LENGTH + " characters");
        }
    }

    private static void validateSlug(String slug) {
        Objects.requireNonNull(slug, "slug must not be null");
        if (slug.isBlank()) {
            throw new IllegalArgumentException("slug must not be blank");
        }
        if (slug.length() > MAX_SLUG_LENGTH) {
            throw new IllegalArgumentException(
                    "slug must not exceed " + MAX_SLUG_LENGTH + " characters");
        }
        // slug 형식 검증 (영문, 숫자, 하이픈만)
        if (!slug.matches("^[a-zA-Z0-9-]+$")) {
            throw new IllegalArgumentException(
                    "slug must contain only letters, numbers, and hyphens: " + slug);
        }
    }
}
