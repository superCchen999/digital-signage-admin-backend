package com.digitalsignage.admin.media;

import com.digitalsignage.admin.auth.repository.SysUserRepository;
import com.digitalsignage.admin.common.enums.MediaType;
import com.digitalsignage.admin.common.enums.OrganizationStatus;
import com.digitalsignage.admin.common.enums.SysUserStatus;
import com.digitalsignage.admin.common.enums.UserRole;
import com.digitalsignage.admin.entity.Media;
import com.digitalsignage.admin.entity.Organization;
import com.digitalsignage.admin.entity.SysUser;
import com.digitalsignage.admin.media.repository.MediaRepository;
import com.digitalsignage.admin.user.repository.OrganizationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * VIEWER may read media; upload/confirm/delete require EDITOR or ADMIN.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MediaViewerIntegrationTest {

    private static final String LOGIN = "/api/admin/auth/login";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MediaRepository mediaRepository;

    private Organization organization;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        organization = new Organization();
        organization.setName("Viewer Org");
        organization.setCode("vorg-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        organization.setStatus(OrganizationStatus.ACTIVE);
        organization.setCreatedAt(now);
        organization.setUpdatedAt(now);
        organizationRepository.save(organization);

        SysUser viewer = new SysUser();
        viewer.setOrganization(organization);
        viewer.setUsername("media_viewer");
        viewer.setPasswordHash(passwordEncoder.encode("Secret123!"));
        viewer.setEmail("viewer@example.com");
        viewer.setRole(UserRole.VIEWER);
        viewer.setStatus(SysUserStatus.ACTIVE);
        viewer.setCreatedAt(now);
        viewer.setUpdatedAt(now);
        sysUserRepository.save(viewer);

        Media media = new Media();
        media.setOrganization(organization);
        media.setMediaType(MediaType.IMAGE);
        media.setName("Public asset");
        media.setObjectKey(organization.getId() + "/seed.png");
        media.setFileUrl("https://cdn/seed.png");
        media.setCreatedAt(now);
        media.setUpdatedAt(now);
        mediaRepository.save(media);
    }

    private String viewerToken() throws Exception {
        MvcResult login = mockMvc.perform(post(LOGIN)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"media_viewer\",\"password\":\"Secret123!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(login.getResponse().getContentAsString());
        return root.path("data").path("accessToken").asText();
    }

    @Test
    void viewer_canListAndGet() throws Exception {
        String token = viewerToken();

        mockMvc.perform(get("/api/admin/media")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("Public asset"));

        Long id = mediaRepository.findAllByOrganizationId(organization.getId()).get(0).getId();

        mockMvc.perform(get("/api/admin/media/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Public asset"));
    }

    @Test
    void viewer_cannotRequestUploadPolicy() throws Exception {
        String token = viewerToken();

        mockMvc.perform(post("/api/admin/media/upload-policy")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"mediaType\":\"IMAGE\",\"originalFilename\":\"x.png\"}"))
                .andExpect(status().isForbidden());
    }
}
