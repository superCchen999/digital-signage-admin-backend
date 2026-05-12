package com.digitalsignage.admin.media.dto;

import com.digitalsignage.admin.common.enums.MediaType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmMediaRequest {

    @NotBlank
    @Size(max = 512)
    private String objectKey;

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    private MediaType mediaType;

    @Size(max = 1024)
    private String fileUrl;

    @Size(max = 1024)
    private String thumbnailUrl;

    @Min(0)
    private Long fileSizeBytes;

    @Min(0)
    private Integer durationSeconds;

    @Size(max = 64)
    private String checksumSha256;
}
