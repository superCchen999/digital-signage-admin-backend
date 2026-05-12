package com.digitalsignage.admin.media.service.impl;

import com.digitalsignage.admin.common.enums.MediaType;
import com.digitalsignage.admin.common.enums.UserRole;
import com.digitalsignage.admin.common.exception.BusinessException;
import com.digitalsignage.admin.entity.Media;
import com.digitalsignage.admin.entity.Organization;
import com.digitalsignage.admin.media.config.MediaStorageProperties;
import com.digitalsignage.admin.media.dto.ConfirmMediaRequest;
import com.digitalsignage.admin.media.dto.UploadPolicyRequest;
import com.digitalsignage.admin.media.dto.UploadPolicyResponse;
import com.digitalsignage.admin.media.repository.MediaRepository;
import com.digitalsignage.admin.playlist.repository.PlaylistItemRepository;
import com.digitalsignage.admin.security.AdminPrincipal;
import com.digitalsignage.admin.user.repository.OrganizationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    private static final Long ORG_ID = 10L;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private PlaylistItemRepository playlistItemRepository;

    @Mock
    private MediaStorageProperties storageProperties;

    @InjectMocks
    private MediaServiceImpl mediaService;

    private Organization organization;

    @BeforeEach
    void setUpPrincipal() {
        organization = new Organization();
        organization.setId(ORG_ID);
        organization.setName("Org");

        AdminPrincipal principal = AdminPrincipal.builder()
                .userId(1L)
                .organizationId(ORG_ID)
                .username("editor")
                .role(UserRole.EDITOR)
                .build();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadPolicy_withoutPresignedTemplate_returnsDeferred() {
        when(storageProperties.getUploadPolicyExpireMinutes()).thenReturn(60);
        when(storageProperties.getPresignedPutUrlTemplate()).thenReturn("");

        UploadPolicyRequest req = new UploadPolicyRequest();
        req.setMediaType(MediaType.IMAGE);
        req.setOriginalFilename("a b.png");

        UploadPolicyResponse res = mediaService.uploadPolicy(req);

        assertThat(res.getUploadMethod()).isEqualTo("DEFERRED");
        assertThat(res.getUploadUrl()).isNull();
        assertThat(res.getObjectKey()).startsWith(ORG_ID + "/");
        assertThat(res.getObjectKey()).endsWith("_a_b.png");
        assertThat(res.getExpiresAt()).isNotNull();
    }

    @Test
    void uploadPolicy_withPresignedTemplate_returnsPutUrl() {
        when(storageProperties.getUploadPolicyExpireMinutes()).thenReturn(30);
        when(storageProperties.getPresignedPutUrlTemplate()).thenReturn("https://bucket/{objectKey}?sign=1");

        UploadPolicyRequest req = new UploadPolicyRequest();
        req.setMediaType(MediaType.VIDEO);
        req.setOriginalFilename("clip.mp4");

        UploadPolicyResponse res = mediaService.uploadPolicy(req);

        assertThat(res.getUploadMethod()).isEqualTo("PUT");
        assertThat(res.getUploadUrl()).contains(res.getObjectKey());
        assertThat(res.getObjectKey()).startsWith(ORG_ID + "/");
    }

    @Test
    void uploadPolicy_youtubeKeyUsesYtPrefix() {
        when(storageProperties.getUploadPolicyExpireMinutes()).thenReturn(60);
        when(storageProperties.getPresignedPutUrlTemplate()).thenReturn("");

        UploadPolicyRequest req = new UploadPolicyRequest();
        req.setMediaType(MediaType.YOUTUBE);
        req.setOriginalFilename("ignored");

        UploadPolicyResponse res = mediaService.uploadPolicy(req);

        assertThat(res.getObjectKey()).startsWith(ORG_ID + "/yt-");
    }

    @Test
    void confirm_buildsFileUrlFromPublicBaseWhenMissing() {
        when(mediaRepository.existsByOrganization_IdAndObjectKey(ORG_ID, "10/key.png")).thenReturn(false);
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(organization));
        when(storageProperties.getPublicBaseUrl()).thenReturn("https://cdn.example.com/");
        when(mediaRepository.save(any(Media.class))).thenAnswer(inv -> {
            Media m = inv.getArgument(0);
            m.setId(50L);
            return m;
        });
        when(mediaRepository.findByIdAndOrganizationId(50L, ORG_ID)).thenAnswer(inv -> {
            Media m = new Media();
            m.setId(50L);
            m.setOrganization(organization);
            m.setMediaType(MediaType.IMAGE);
            m.setName("n");
            m.setObjectKey("10/key.png");
            m.setFileUrl("https://cdn.example.com/10/key.png");
            return Optional.of(m);
        });

        ConfirmMediaRequest req = new ConfirmMediaRequest();
        req.setObjectKey("10/key.png");
        req.setName("n");
        req.setMediaType(MediaType.IMAGE);

        assertThat(mediaService.confirm(req).getFileUrl()).isEqualTo("https://cdn.example.com/10/key.png");
    }

    @Test
    void confirm_duplicateObjectKey_throws400() {
        when(mediaRepository.existsByOrganization_IdAndObjectKey(ORG_ID, "10/dup")).thenReturn(true);

        ConfirmMediaRequest req = new ConfirmMediaRequest();
        req.setObjectKey("10/dup");
        req.setName("x");
        req.setMediaType(MediaType.IMAGE);

        assertThatThrownBy(() -> mediaService.confirm(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(400);
        verify(organizationRepository, never()).findById(any());
    }

    @Test
    void confirm_wrongOrgPrefix_throws400() {
        ConfirmMediaRequest req = new ConfirmMediaRequest();
        req.setObjectKey("99/other");
        req.setName("x");
        req.setMediaType(MediaType.IMAGE);

        assertThatThrownBy(() -> mediaService.confirm(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(400);
    }

    @Test
    void confirm_objectKeyWithDotDot_throws400() {
        ConfirmMediaRequest req = new ConfirmMediaRequest();
        req.setObjectKey("10/../escape");
        req.setName("x");
        req.setMediaType(MediaType.IMAGE);

        assertThatThrownBy(() -> mediaService.confirm(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(400);
    }

    @Test
    void getMedia_notFound_throws404() {
        when(mediaRepository.findByIdAndOrganizationId(3L, ORG_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mediaService.getMedia(3L))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(404);
    }

    @Test
    void deleteMedia_whenReferencedByPlaylist_throws400() {
        Media media = new Media();
        media.setId(8L);
        media.setOrganization(organization);
        when(mediaRepository.findByIdAndOrganizationId(8L, ORG_ID)).thenReturn(Optional.of(media));
        when(playlistItemRepository.existsByMedia_Id(8L)).thenReturn(true);

        assertThatThrownBy(() -> mediaService.deleteMedia(8L))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(400);
        verify(mediaRepository, never()).delete(any());
    }

    @Test
    void currentPrincipal_missing_throws401() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> mediaService.listMedia())
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(401);
    }
}
