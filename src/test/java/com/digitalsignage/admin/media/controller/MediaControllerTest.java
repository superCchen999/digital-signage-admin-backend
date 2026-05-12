package com.digitalsignage.admin.media.controller;

import com.digitalsignage.admin.common.enums.MediaType;
import com.digitalsignage.admin.common.exception.GlobalExceptionHandler;
import com.digitalsignage.admin.media.dto.ConfirmMediaRequest;
import com.digitalsignage.admin.media.dto.MediaResponse;
import com.digitalsignage.admin.media.dto.UploadPolicyRequest;
import com.digitalsignage.admin.media.dto.UploadPolicyResponse;
import com.digitalsignage.admin.media.service.MediaService;
import com.digitalsignage.admin.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MediaController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@WithMockUser(roles = {"EDITOR"})
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MediaService mediaService;

    @MockBean
    private JwtService jwtService;

    @Test
    void uploadPolicy_returnsOk() throws Exception {
        UploadPolicyResponse body = UploadPolicyResponse.builder()
                .objectKey("10/uuid-file.mp4")
                .uploadMethod("PUT")
                .uploadUrl("https://example.com/presigned")
                .expiresAt(Instant.parse("2026-01-01T00:00:00Z"))
                .requiredHeaders(Map.of("Content-Type", "video/mp4"))
                .build();
        when(mediaService.uploadPolicy(any(UploadPolicyRequest.class))).thenReturn(body);

        UploadPolicyRequest req = new UploadPolicyRequest();
        req.setMediaType(MediaType.VIDEO);
        req.setOriginalFilename("clip.mp4");

        mockMvc.perform(post("/api/admin/media/upload-policy")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.objectKey").value("10/uuid-file.mp4"))
                .andExpect(jsonPath("$.data.uploadMethod").value("PUT"));
        verify(mediaService).uploadPolicy(any(UploadPolicyRequest.class));
    }

    @Test
    void uploadPolicy_missingFilename_returns400() throws Exception {
        mockMvc.perform(post("/api/admin/media/upload-policy")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"mediaType\":\"IMAGE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void confirm_returnsOk() throws Exception {
        MediaResponse saved = MediaResponse.builder()
                .id(1L)
                .organizationId(10L)
                .mediaType(MediaType.IMAGE)
                .name("banner")
                .objectKey("10/uuid.png")
                .fileUrl("https://cdn/x.png")
                .build();
        when(mediaService.confirm(any(ConfirmMediaRequest.class))).thenReturn(saved);

        ConfirmMediaRequest req = new ConfirmMediaRequest();
        req.setObjectKey("10/uuid.png");
        req.setName("banner");
        req.setMediaType(MediaType.IMAGE);

        mockMvc.perform(post("/api/admin/media/confirm")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void list_returnsOk() throws Exception {
        when(mediaService.listMedia()).thenReturn(List.of(
                MediaResponse.builder()
                        .id(2L)
                        .organizationId(10L)
                        .mediaType(MediaType.VIDEO)
                        .name("ad")
                        .objectKey("10/v.mp4")
                        .build()));

        mockMvc.perform(get("/api/admin/media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("ad"));
    }

    @Test
    void get_returnsOk() throws Exception {
        when(mediaService.getMedia(5L)).thenReturn(
                MediaResponse.builder()
                        .id(5L)
                        .organizationId(10L)
                        .mediaType(MediaType.IMAGE)
                        .name("x")
                        .objectKey("10/x.png")
                        .build());

        mockMvc.perform(get("/api/admin/media/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));
        verify(mediaService).getMedia(eq(5L));
    }

    @Test
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/admin/media/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        verify(mediaService).deleteMedia(7L);
    }
}
