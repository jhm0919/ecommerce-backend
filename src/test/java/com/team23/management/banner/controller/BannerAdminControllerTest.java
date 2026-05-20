package com.team23.management.banner.controller;

import com.team23.management.banner.dto.BannerCreateRequest;
import com.team23.management.banner.repository.BannerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BannerAdminControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    BannerRepository bannerRepository;

    @BeforeEach
    void setUp() {
        bannerRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        bannerRepository.deleteAll();
    }

    private BannerCreateRequest createRequest(int displayOrder) {
        return new BannerCreateRequest(
                "여름 세일",
                "https://cdn.example.com/banner1.jpg",
                "https://example.com/sale",
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 6, 30, 23, 59),
                displayOrder
        );
    }

    @Test
    @DisplayName("POST /api/admin/banners — 정상 등록 201")
    void createSuccessReturns201() throws Exception {
        BannerCreateRequest request = createRequest(1);

        mockMvc.perform(post("/api/admin/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.bannerId").exists());
    }

    @Test
    @DisplayName("displayOrder 중복 → 409")
    void createDuplicateOrderReturns409() throws Exception {
        BannerCreateRequest request = createRequest(1);
        mockMvc.perform(post("/api/admin/banners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()));

        mockMvc.perform(post("/api/admin/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("필수 항목 누락 → 400")
    void createMissingFieldReturns400() throws Exception {
        // name 누락
        String body = """
                {
                  "imageUrl": "https://cdn.example.com/banner1.jpg",
                  "linkUrl": "https://example.com/sale",
                  "startAt": "2026-06-01T00:00:00",
                  "endAt": "2026-06-30T23:59:59",
                  "displayOrder": 1
                }
                """;

        mockMvc.perform(post("/api/admin/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미지 URL 형식 오류 → 400")
    void createInvalidImageUrlReturns400() throws Exception {
        BannerCreateRequest invalid = new BannerCreateRequest(
                "여름 세일",
                "not-a-url",
                "https://example.com/sale",
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 6, 30, 23, 59),
                1
        );

        mockMvc.perform(post("/api/admin/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시 기간 오류 → 400")
    void createInvalidPeriodReturns400() throws Exception {
        BannerCreateRequest invalid = new BannerCreateRequest(
                "여름 세일",
                "https://cdn.example.com/banner1.jpg",
                "https://example.com/sale",
                LocalDateTime.of(2026, 6, 30, 23, 59),
                LocalDateTime.of(2026, 6, 1, 0, 0),
                1
        );

        mockMvc.perform(post("/api/admin/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}