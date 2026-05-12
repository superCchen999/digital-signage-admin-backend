package com.digitalsignage.admin.media.dto;

import com.digitalsignage.admin.common.enums.MediaType;
import com.digitalsignage.admin.entity.Media;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MediaResponse {

    private Long id;
    private Long organizationId;
    private MediaType mediaType;
    private String name;
    private String objectKey;
    private String fileUrl;
    private String thumbnailUrl;
    private Long fileSizeBytes;
    private Integer durationSeconds;
    private String checksumSha256;
    private LocalDateTime createdAt;

    public static MediaResponse fromEntity(Media media) {
        return MediaResponse.builder()
                .id(media.getId())
                .organizationId(media.getOrganization().getId())
                .mediaType(media.getMediaType())
                .name(media.getName())
                .objectKey(media.getObjectKey())
                .fileUrl(media.getFileUrl())
                .thumbnailUrl(media.getThumbnailUrl())
                .fileSizeBytes(media.getFileSizeBytes())
                .durationSeconds(media.getDurationSeconds())
                .checksumSha256(media.getChecksumSha256())
                .createdAt(media.getCreatedAt())
                .build();
    }
}
