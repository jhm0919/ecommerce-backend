package com.team23.management.banner.controller;

import com.team23.management.banner.dto.BannerCreateRequest;
import com.team23.management.banner.dto.BannerUpdateRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    private BannerUpdateRequest updateRequest(String name, int displayOrder) {
        return new BannerUpdateRequest(
                name,
                "https://cdn.example.com/banner-updated.jpg",
                "https://example.com/updated",
                LocalDateTime.of(2026, 7, 1, 0, 0),
                LocalDateTime.of(2026, 7, 31, 23, 59),
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

    @Test
    @DisplayName("GET /api/admin/banners — 목록 조회 200")
    void list_returns200() throws Exception {
        // 사전 데이터
        mockMvc.perform(post("/api/admin/banners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest(1)))
                .with(csrf()));

        mockMvc.perform(get("/api/admin/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("DRAFT"));
    }

    @Test
    @DisplayName("GET /api/admin/banners?status=DRAFT — 필터 정상 동작")
    void listWithStatus_returns200() throws Exception {
        mockMvc.perform(post("/api/admin/banners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest(1)))
                .with(csrf()));

        mockMvc.perform(get("/api/admin/banners")
                        .param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/admin/banners?status=PUBLISHED — DRAFT만 있으면 빈 목록")
    void listFilterPublished_returnsEmptyWhenAllDraft() throws Exception {
        mockMvc.perform(post("/api/admin/banners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest(1)))
                .with(csrf()));

        mockMvc.perform(get("/api/admin/banners")
                        .param("status", "PUBLISHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/admin/banners — 빈 목록도 200")
    void listEmpty_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/admin/banners/{id} - 정상 조회 200")
    void getOne_returns200() throws Exception {
        // 사전 데이터 생성
        String createResponse = mockMvc.perform(post("/api/admin/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(1)))
                        .with(csrf()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long bannerId = objectMapper.readTree(createResponse)
                .path("data").path("bannerId").asLong();

        mockMvc.perform(get("/api/admin/banners/{id}", bannerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.bannerId").value(bannerId))
                .andExpect(jsonPath("$.data.name").value("여름 세일"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    @DisplayName("GET /api/admin/banners/{id} - 존재하지 않는 ID → 404")
    void getOneNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/admin/banners/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/admin/banners/{id} - 정상 수정 200")
    void update_returns200() throws Exception {
        // 사전 데이터
        String createResponse = mockMvc.perform(post("/api/admin/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(1)))
                        .with(csrf()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long bannerId = objectMapper.readTree(createResponse)
                .path("data").path("bannerId").asLong();

        mockMvc.perform(put("/api/admin/banners/{id}", bannerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest("가을 세일", 1)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("PUT - 존재하지 않는 ID → 404")
    void updateNotFound_returns404() throws Exception {
        mockMvc.perform(put("/api/admin/banners/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest("가을 세일", 1)))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT - 다른 배너와 displayOrder 중복 → 409")
    void updateDuplicateOrder_returns409() throws Exception {
        String createResponse = mockMvc.perform(post("/api/admin/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(1)))
                        .with(csrf()))
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post("/api/admin/banners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest(2)))
                .with(csrf()));

        Long bannerId2 = bannerRepository.findAllByOrderByDisplayOrderAsc()
                .get(1).getId();   // displayOrder=2 의 배너

        // bannerId2 를 displayOrder=1 로 수정 시도 → 중복
        mockMvc.perform(put("/api/admin/banners/{id}", bannerId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest("가을 세일", 1)))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT - 필수 항목 누락 → 400")
    void updateMissingField_returns400() throws Exception {
        String createResponse = mockMvc.perform(post("/api/admin/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(1)))
                        .with(csrf()))
                .andReturn().getResponse().getContentAsString();
        Long bannerId = objectMapper.readTree(createResponse)
                .path("data").path("bannerId").asLong();

        String invalidBody = """
            {
              "imageUrl": "https://cdn.example.com/banner-updated.jpg",
              "linkUrl": "https://example.com/updated",
              "startAt": "2026-07-01T00:00:00",
              "endAt": "2026-07-31T23:59:59",
              "displayOrder": 1
            }
            """;

        mockMvc.perform(put("/api/admin/banners/{id}", bannerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/admin/banners/{id}/publish - 정상 게시 200")
    void publish_returns200() throws Exception {
        String createResponse = mockMvc.perform(post("/api/admin/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(1)))
                        .with(csrf()))
                .andReturn().getResponse().getContentAsString();
        Long bannerId = objectMapper.readTree(createResponse)
                .path("data").path("bannerId").asLong();

        mockMvc.perform(patch("/api/admin/banners/{id}/publish", bannerId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("PATCH publish - 이미 게시된 배너 → 400")
    void publishAlreadyPublished_returns400() throws Exception {
        String createResponse = mockMvc.perform(post("/api/admin/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(1)))
                        .with(csrf()))
                .andReturn().getResponse().getContentAsString();
        Long bannerId = objectMapper.readTree(createResponse)
                .path("data").path("bannerId").asLong();

        mockMvc.perform(patch("/api/admin/banners/{id}/publish", bannerId)
                .with(csrf()));

        mockMvc.perform(patch("/api/admin/banners/{id}/publish", bannerId)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH publish - 존재하지 않는 ID → 404")
    void publishNotFound_returns404() throws Exception {
        mockMvc.perform(patch("/api/admin/banners/{id}/publish", 999L)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}