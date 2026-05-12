package com.digitalsignage.admin.media.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
public class UploadPolicyResponse {

    private String objectKey;
    private String uploadMethod;
    private String uploadUrl;
    private Instant expiresAt;
    private Map<String, String> requiredHeaders;
}
