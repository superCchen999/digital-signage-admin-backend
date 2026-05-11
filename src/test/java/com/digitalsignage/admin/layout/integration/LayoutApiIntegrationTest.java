package com.digitalsignage.admin.layout.integration;

import com.digitalsignage.admin.common.enums.LayoutStatus;
import com.digitalsignage.admin.common.enums.OrganizationStatus;
import com.digitalsignage.admin.common.enums.UserRole;
import com.digitalsignage.admin.entity.Organization;
import com.digitalsignage.admin.layout.dto.CreateLayoutRequest;
import com.digitalsignage.admin.layout.dto.LayoutRegionRequest;
import com.digitalsignage.admin.layout.dto.UpdateLayoutRequest;
import com.digitalsignage.admin.layout.repository.LayoutRegionRepository;
import com.digitalsignage.admin.layout.repository.LayoutRepository;
import com.digitalsignage.admin.security.AdminPrincipal;
import com.digitalsignage.admin.security.JwtService;
import com.digitalsignage.admin.user.repository.OrganizationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LayoutApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private LayoutRepository layoutRepository;

    @Autowired
    private LayoutRegionRepository layoutRegionRepository;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        layoutRegionRepository.deleteAll();
        layoutRepository.deleteAll();
        organizationRepository.deleteAll();

        Organization organization = new Organization();
        organization.setName("Org A");
        organization.setCode("ORG_A");
        organization.setStatus(OrganizationStatus.ACTIVE);
        Organization savedOrg = organizationRepository.save(organization);

        AdminPrincipal principal = AdminPrincipal.builder()
                .userId(1L)
                .organizationId(savedOrg.getId())
                .username("admin")
                .role(UserRole.ADMIN)
                .build();
        when(jwtService.parseAccessToken("it-token")).thenReturn(principal);
    }

    @Test
    void createGetUpdateDelete_layout_success() throws Exception {
        CreateLayoutRequest createBody = new CreateLayoutRequest();
        createBody.setName("Lobby Layout");
        createBody.setTemplateType("SINGLE_FULL");
        createBody.setResolutionWidth(1920);
        createBody.setResolutionHeight(1080);
        createBody.setStatus(LayoutStatus.DRAFT);
        createBody.setRegions(List.of(region("main", "PLAYLIST", "{\"playlistId\":1}", 1080)));

        String createResponse = mockMvc.perform(post("/api/admin/layouts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer it-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Lobby Layout"))
                .andExpect(jsonPath("$.data.regions[0].regionName").value("main"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long layoutId = objectMapper.readTree(createResponse).path("data").path("id").asLong();
        assertThat(layoutId).isPositive();

        mockMvc.perform(get("/api/admin/layouts/{id}", layoutId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer it-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(layoutId))
                .andExpect(jsonPath("$.data.templateType").value("SINGLE_FULL"));

        mockMvc.perform(get("/api/admin/layouts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer it-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(layoutId));

        UpdateLayoutRequest updateBody = new UpdateLayoutRequest();
        updateBody.setName("Lobby Layout Updated");
        updateBody.setStatus(LayoutStatus.PUBLISHED);
        updateBody.setRegions(List.of(region("top", "TEXT", "{\"text\":\"hello\"}", 200)));

        mockMvc.perform(put("/api/admin/layouts/{id}", layoutId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer it-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Lobby Layout Updated"))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.regions[0].componentType").value("TEXT"));

        mockMvc.perform(delete("/api/admin/layouts/{id}", layoutId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer it-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/admin/layouts/{id}", layoutId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer it-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void listTemplates_requiresAuthAndReturnsData() throws Exception {
        mockMvc.perform(get("/api/admin/layout-templates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer it-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].templateType").exists());
    }

    private static LayoutRegionRequest region(String regionName, String componentType, String configJson, int height) {
        LayoutRegionRequest request = new LayoutRegionRequest();
        request.setRegionName(regionName);
        request.setX(0);
        request.setY(0);
        request.setWidth(1920);
        request.setHeight(height);
        request.setZIndex(1);
        request.setComponentType(componentType);
        request.setConfigJson(configJson);
        return request;
    }
}
