package com.digitalsignage.admin.layout.controller;

import com.digitalsignage.admin.common.enums.LayoutStatus;
import com.digitalsignage.admin.common.exception.GlobalExceptionHandler;
import com.digitalsignage.admin.layout.dto.CreateLayoutRequest;
import com.digitalsignage.admin.layout.dto.LayoutRegionRequest;
import com.digitalsignage.admin.layout.dto.LayoutRegionResponse;
import com.digitalsignage.admin.layout.dto.LayoutResponse;
import com.digitalsignage.admin.layout.dto.LayoutTemplateResponse;
import com.digitalsignage.admin.layout.dto.UpdateLayoutRequest;
import com.digitalsignage.admin.layout.service.LayoutService;
import com.digitalsignage.admin.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LayoutController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
class LayoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LayoutService layoutService;

    @MockBean
    private JwtService jwtService;

    private static LayoutResponse sampleLayout() {
        return LayoutResponse.builder()
                .id(1L)
                .organizationId(10L)
                .name("Lobby Layout")
                .templateType("SINGLE_FULL")
                .resolutionWidth(1920)
                .resolutionHeight(1080)
                .status(LayoutStatus.PUBLISHED)
                .createdAt(LocalDateTime.parse("2026-01-01T10:00:00"))
                .updatedAt(LocalDateTime.parse("2026-01-02T10:00:00"))
                .regions(List.of(
                        LayoutRegionResponse.builder()
                                .id(100L)
                                .regionName("main")
                                .x(0)
                                .y(0)
                                .width(1920)
                                .height(1080)
                                .zIndex(1)
                                .componentType("PLAYLIST")
                                .configJson("{\"playlistId\":1}")
                                .build()
                ))
                .build();
    }

    @Test
    void listTemplates_returnsOk() throws Exception {
        when(layoutService.listTemplates()).thenReturn(List.of(
                LayoutTemplateResponse.builder().templateType("SINGLE_FULL").displayName("Single Full").build()
        ));

        mockMvc.perform(get("/api/admin/layout-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].templateType").value("SINGLE_FULL"));
    }

    @Test
    void listLayouts_returnsOk() throws Exception {
        when(layoutService.listLayouts()).thenReturn(List.of(sampleLayout()));

        mockMvc.perform(get("/api/admin/layouts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Lobby Layout"));
    }

    @Test
    void getLayout_returnsOk() throws Exception {
        when(layoutService.getLayout(1L)).thenReturn(sampleLayout());

        mockMvc.perform(get("/api/admin/layouts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.regions[0].regionName").value("main"));
    }

    @Test
    void createLayout_returnsOk() throws Exception {
        when(layoutService.createLayout(any(CreateLayoutRequest.class))).thenReturn(sampleLayout());

        CreateLayoutRequest request = new CreateLayoutRequest();
        request.setName("Lobby Layout");
        request.setTemplateType("SINGLE_FULL");
        request.setResolutionWidth(1920);
        request.setResolutionHeight(1080);
        request.setStatus(LayoutStatus.DRAFT);
        request.setRegions(List.of(sampleRegionRequest()));

        mockMvc.perform(post("/api/admin/layouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void createLayout_missingRegions_returnsBadRequest() throws Exception {
        CreateLayoutRequest request = new CreateLayoutRequest();
        request.setName("Lobby Layout");
        request.setTemplateType("SINGLE_FULL");
        request.setResolutionWidth(1920);
        request.setResolutionHeight(1080);
        request.setStatus(LayoutStatus.DRAFT);

        mockMvc.perform(post("/api/admin/layouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void updateLayout_returnsOk() throws Exception {
        when(layoutService.updateLayout(eq(1L), any(UpdateLayoutRequest.class))).thenReturn(sampleLayout());

        UpdateLayoutRequest request = new UpdateLayoutRequest();
        request.setName("Lobby Layout New");
        request.setRegions(List.of(sampleRegionRequest()));

        mockMvc.perform(put("/api/admin/layouts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        verify(layoutService).updateLayout(eq(1L), any(UpdateLayoutRequest.class));
    }

    @Test
    void deleteLayout_returnsOk() throws Exception {
        doNothing().when(layoutService).deleteLayout(2L);

        mockMvc.perform(delete("/api/admin/layouts/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        verify(layoutService).deleteLayout(2L);
    }

    private static LayoutRegionRequest sampleRegionRequest() {
        LayoutRegionRequest region = new LayoutRegionRequest();
        region.setRegionName("main");
        region.setX(0);
        region.setY(0);
        region.setWidth(1920);
        region.setHeight(1080);
        region.setZIndex(1);
        region.setComponentType("PLAYLIST");
        region.setConfigJson("{\"playlistId\":1}");
        return region;
    }
}
