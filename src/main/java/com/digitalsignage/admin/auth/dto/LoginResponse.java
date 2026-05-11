package com.digitalsignage.admin.auth.dto;

import com.digitalsignage.admin.common.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long userId;
    private String username;
    private UserRole role;
    private Long organizationId;
}
