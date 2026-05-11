package com.digitalsignage.admin.auth.service.impl;

import com.digitalsignage.admin.auth.dto.LoginRequest;
import com.digitalsignage.admin.auth.dto.LoginResponse;
import com.digitalsignage.admin.auth.dto.RefreshTokenRequest;
import com.digitalsignage.admin.auth.repository.SysUserRepository;
import com.digitalsignage.admin.auth.service.AuthService;
import com.digitalsignage.admin.common.enums.SysUserStatus;
import com.digitalsignage.admin.common.exception.BusinessException;
import com.digitalsignage.admin.entity.SysUser;
import com.digitalsignage.admin.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "invalid username or password");
        }

        if (user.getStatus() != SysUserStatus.ACTIVE) {
            throw new BusinessException(403, "user is disabled");
        }

        return LoginResponse.builder()
                .accessToken(jwtService.createAccessToken(user))
                .refreshToken(jwtService.createRefreshToken(user))
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .organizationId(user.getOrganization().getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse refresh(RefreshTokenRequest request) {
        Long userId = jwtService.parseRefreshTokenUserId(request.getRefreshToken());
        SysUser user = sysUserRepository.findByIdWithOrganization(userId)
                .orElseThrow(() -> new BusinessException(401, "invalid token"));

        if (user.getStatus() != SysUserStatus.ACTIVE) {
            throw new BusinessException(403, "user is disabled");
        }

        return LoginResponse.builder()
                .accessToken(jwtService.createAccessToken(user))
                .refreshToken(jwtService.createRefreshToken(user))
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .organizationId(user.getOrganization().getId())
                .build();
    }
}
