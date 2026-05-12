package com.digitalsignage.admin.media.dto;

import com.digitalsignage.admin.common.enums.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadPolicyRequest {

    @NotNull
    private MediaType mediaType;

    @NotBlank
    @Size(max = 255)
    private String originalFilename;

    @Size(max = 128)
    private String contentType;

    private Long fileSizeBytes;
}
