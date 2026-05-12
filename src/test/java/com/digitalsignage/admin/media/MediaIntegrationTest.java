package com.digitalsignage.admin.media;

import com.digitalsignage.admin.auth.repository.SysUserRepository;
import com.digitalsignage.admin.common.enums.OrganizationStatus;
import com.digitalsignage.admin.common.enums.PlaylistStatus;
import com.digitalsignage.admin.common.enums.SysUserStatus;
import com.digitalsignage.admin.common.enums.UserRole;
import com.digitalsignage.admin.entity.Media;
import com.digitalsignage.admin.entity.Organization;
import com.digitalsignage.admin.entity.Playlist;
import com.digitalsignage.admin.entity.PlaylistItem;
import com.digitalsignage.admin.entity.SysUser;
import com.digitalsignage.admin.media.repository.MediaRepository;
import com.digitalsignage.admin.user.repository.OrganizationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MediaIntegrationTest {

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

    @Autowired
    private EntityManager entityManager;

    private Organization organization;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        organization = new Organization();
        organization.setName("Media IT Org");
        organization.setCode("morg-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        organization.setStatus(OrganizationStatus.ACTIVE);
        organization.setCreatedAt(now);
        organization.setUpdatedAt(now);
        organizationRepository.save(organization);

        SysUser user = new SysUser();
        user.setOrganization(organization);
        user.setUsername("media_it_editor");
        user.setPasswordHash(passwordEncoder.encode("Secret123!"));
        user.setEmail("media_it@example.com");
        user.setRole(UserRole.EDITOR);
        user.setStatus(SysUserStatus.ACTIVE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        sysUserRepository.save(user);
    }

    private String accessToken() throws Exception {
        MvcResult login = mockMvc.perform(post(LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"media_it_editor\",\"password\":\"Secret123!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(login.getResponse().getContentAsString());
        return root.path("data").path("accessToken").asText();
    }

    @Test
    void mediaFlow_uploadPolicy_confirm_list_get_delete() throws Exception {
        String token = accessToken();

        MvcResult policyRes = mockMvc.perform(post("/api/admin/media/upload-policy")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mediaType\":\"IMAGE\",\"originalFilename\":\"banner.png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.objectKey").exists())
                .andExpect(jsonPath("$.data.uploadMethod").value("DEFERRED"))
                .andReturn();

        String objectKey = objectMapper.readTree(policyRes.getResponse().getContentAsString())
                .path("data").path("objectKey").asText();
        assertThat(objectKey).startsWith(organization.getId() + "/");

        String confirmBody = objectMapper.writeValueAsString(Map.of(
                "objectKey", objectKey,
                "name", "Banner",
                "mediaType", "IMAGE",
                "fileUrl", "https://cdn.example.com/" + objectKey));

        MvcResult confirmRes = mockMvc.perform(post("/api/admin/media/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("Banner"))
                .andReturn();

        long mediaId = objectMapper.readTree(confirmRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(get("/api/admin/media")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value((int) mediaId))
                .andExpect(jsonPath("$.data[0].objectKey").value(objectKey));

        mockMvc.perform(get("/api/admin/media/" + mediaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Banner"));

        mockMvc.perform(delete("/api/admin/media/" + mediaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/admin/media/" + mediaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void confirm_wrongObjectKeyPrefix_returns400() throws Exception {
        String token = accessToken();
        mockMvc.perform(post("/api/admin/media/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "objectKey", "999/wrong-key",
                                "name", "x",
                                "mediaType", "IMAGE"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void confirm_duplicateObjectKey_returns400() throws Exception {
        String token = accessToken();

        MvcResult policyRes = mockMvc.perform(post("/api/admin/media/upload-policy")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mediaType\":\"IMAGE\",\"originalFilename\":\"dup.png\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String objectKey = objectMapper.readTree(policyRes.getResponse().getContentAsString())
                .path("data").path("objectKey").asText();

        String confirmBody = objectMapper.writeValueAsString(Map.of(
                "objectKey", objectKey,
                "name", "First",
                "mediaType", "IMAGE",
                "fileUrl", "https://cdn.example.com/" + objectKey));

        mockMvc.perform(post("/api/admin/media/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/media/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void delete_whenMediaReferencedByPlaylist_returns400() throws Exception {
        String token = accessToken();

        MvcResult policyRes = mockMvc.perform(post("/api/admin/media/upload-policy")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mediaType\":\"VIDEO\",\"originalFilename\":\"ref.mp4\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String objectKey = objectMapper.readTree(policyRes.getResponse().getContentAsString())
                .path("data").path("objectKey").asText();

        MvcResult confirmRes = mockMvc.perform(post("/api/admin/media/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "objectKey", objectKey,
                                "name", "Locked",
                                "mediaType", "VIDEO",
                                "fileUrl", "https://cdn.example.com/" + objectKey))))
                .andExpect(status().isOk())
                .andReturn();
        long mediaId = objectMapper.readTree(confirmRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        LocalDateTime now = LocalDateTime.now();
        Playlist playlist = new Playlist();
        playlist.setOrganization(organization);
        playlist.setName("pl-ref");
        playlist.setStatus(PlaylistStatus.ACTIVE);
        playlist.setCreatedAt(now);
        playlist.setUpdatedAt(now);
        entityManager.persist(playlist);

        Media media = mediaRepository.findById(mediaId).orElseThrow();
        PlaylistItem item = new PlaylistItem();
        item.setPlaylist(playlist);
        item.setMedia(media);
        item.setOrderIndex(0);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        entityManager.persist(item);
        entityManager.flush();
        mockMvc.perform(delete("/api/admin/media/" + mediaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
