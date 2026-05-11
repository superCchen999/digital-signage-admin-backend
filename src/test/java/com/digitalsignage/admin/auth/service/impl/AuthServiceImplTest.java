package com.digitalsignage.admin.auth.service.impl;

import com.digitalsignage.admin.auth.dto.LoginRequest;
import com.digitalsignage.admin.auth.dto.LoginResponse;
import com.digitalsignage.admin.auth.dto.RefreshTokenRequest;
import com.digitalsignage.admin.auth.repository.SysUserRepository;
import com.digitalsignage.admin.common.enums.SysUserStatus;
import com.digitalsignage.admin.common.enums.UserRole;
import com.digitalsignage.admin.common.exception.BusinessException;
import com.digitalsignage.admin.entity.Organization;
import com.digitalsignage.admin.entity.SysUser;
import com.digitalsignage.admin.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private SysUserRepository sysUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private SysUser activeUser;

    @BeforeEach
    void setUp() {
        Organization org = new Organization();
        org.setId(10L);

        activeUser = new SysUser();
        activeUser.setId(1L);
        activeUser.setOrganization(org);
        activeUser.setUsername("admin");
        activeUser.setPasswordHash("encoded");
        activeUser.setRole(UserRole.ADMIN);
        activeUser.setStatus(SysUserStatus.ACTIVE);
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("plain");

        when(sysUserRepository.findByUsername("admin")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("plain", "encoded")).thenReturn(true);
        when(jwtService.createAccessToken(activeUser)).thenReturn("at");
        when(jwtService.createRefreshToken(activeUser)).thenReturn("rt");

        LoginResponse res = authService.login(req);

        assertThat(res.getAccessToken()).isEqualTo("at");
        assertThat(res.getRefreshToken()).isEqualTo("rt");
        assertThat(res.getTokenType()).isEqualTo("Bearer");
        assertThat(res.getUserId()).isEqualTo(1L);
        assertThat(res.getOrganizationId()).isEqualTo(10L);
    }

    @Test
    void login_unknownUser_throws401() {
        LoginRequest req = new LoginRequest();
        req.setUsername("nope");
        req.setPassword("x");
        when(sysUserRepository.findByUsername("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 401);
    }

    @Test
    void login_badPassword_throws401() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrong");
        when(sysUserRepository.findByUsername("admin")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 401);
    }

    @Test
    void login_disabledUser_throws403() {
        activeUser.setStatus(SysUserStatus.DISABLED);
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("plain");
        when(sysUserRepository.findByUsername("admin")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("plain", "encoded")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 403);
    }

    @Test
    void refresh_success() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("refresh-jwt");

        when(jwtService.parseRefreshTokenUserId("refresh-jwt")).thenReturn(1L);
        when(sysUserRepository.findByIdWithOrganization(1L)).thenReturn(Optional.of(activeUser));
        when(jwtService.createAccessToken(activeUser)).thenReturn("at2");
        when(jwtService.createRefreshToken(activeUser)).thenReturn("rt2");

        LoginResponse res = authService.refresh(req);

        assertThat(res.getAccessToken()).isEqualTo("at2");
        assertThat(res.getRefreshToken()).isEqualTo("rt2");
    }

    @Test
    void refresh_userMissing_throws401() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("refresh-jwt");
        when(jwtService.parseRefreshTokenUserId("refresh-jwt")).thenReturn(99L);
        when(sysUserRepository.findByIdWithOrganization(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 401);
    }

    @Test
    void refresh_disabledUser_throws403() {
        activeUser.setStatus(SysUserStatus.DISABLED);
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("refresh-jwt");
        when(jwtService.parseRefreshTokenUserId("refresh-jwt")).thenReturn(1L);
        when(sysUserRepository.findByIdWithOrganization(1L)).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.refresh(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", 403);
    }
}
