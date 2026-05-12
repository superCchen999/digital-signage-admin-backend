package com.digitalsignage.admin.media.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Optional object storage integration. When {@code presignedPutUrlTemplate} is empty,
 * upload-policy still returns {@code objectKey} for confirm; clients use their own OSS flow.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.storage")
public class MediaStorageProperties {

    /**
     * Public base URL (no trailing slash), e.g. CDN origin. Used to build default {@code fileUrl} on confirm.
     */
    private String publicBaseUrl = "";

    /**
     * If set, may contain literal {@code {objectKey}} replaced with URL-encoded key for presigned PUT flows.
     */
    private String presignedPutUrlTemplate = "";

    @Min(5)
    @Max(10080)
    private int uploadPolicyExpireMinutes = 60;
}
