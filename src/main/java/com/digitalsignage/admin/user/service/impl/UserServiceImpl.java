package com.digitalsignage.admin.user.service.impl;

import com.digitalsignage.admin.auth.repository.SysUserRepository;
import com.digitalsignage.admin.common.enums.SysUserStatus;
import com.digitalsignage.admin.common.exception.BusinessException;
import com.digitalsignage.admin.entity.Organization;
import com.digitalsignage.admin.entity.SysUser;
import com.digitalsignage.admin.security.AdminPrincipal;
import com.digitalsignage.admin.user.dto.CreateUserRequest;
import com.digitalsignage.admin.user.dto.UpdateUserRequest;
import com.digitalsignage.admin.user.dto.UserResponse;
import com.digitalsignage.admin.user.repository.OrganizationRepository;
import com.digitalsignage.admin.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserRepository sysUserRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMe() {
        AdminPrincipal principal = currentPrincipal();
        SysUser user = sysUserRepository.findByIdWithOrganization(principal.getUserId())
                .orElseThrow(() -> new BusinessException(404, "user not found"));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        Long orgId = currentPrincipal().getOrganizationId();
        return sysUserRepository.findByOrganization_IdOrderByUsernameAsc(orgId).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        Long orgId = currentPrincipal().getOrganizationId();
        SysUser user = sysUserRepository.findByIdAndOrganization_Id(id, orgId)
                .orElseThrow(() -> new BusinessException(404, "user not found"));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        AdminPrincipal principal = currentPrincipal();
        Long orgId = principal.getOrganizationId();

        if (sysUserRepository.existsByOrganization_IdAndUsername(orgId, request.getUsername())) {
            throw new BusinessException(400, "username already exists");
        }

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new BusinessException(400, "organization not found"));

        SysUser user = new SysUser();
        user.setOrganization(organization);
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setStatus(SysUserStatus.ACTIVE);

        SysUser saved = sysUserRepository.save(user);
        return UserResponse.fromEntity(
                sysUserRepository.findByIdWithOrganization(saved.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        AdminPrincipal principal = currentPrincipal();
        Long orgId = principal.getOrganizationId();

        SysUser user = sysUserRepository.findByIdAndOrganization_Id(id, orgId)
                .orElseThrow(() -> new BusinessException(404, "user not found"));

        if (request.getEmail() != null) {
            user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail() : null);
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        SysUser saved = sysUserRepository.save(user);
        return UserResponse.fromEntity(
                sysUserRepository.findByIdWithOrganization(saved.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        AdminPrincipal principal = currentPrincipal();
        if (principal.getUserId().equals(id)) {
            throw new BusinessException(400, "cannot delete yourself");
        }
        Long orgId = principal.getOrganizationId();
        SysUser user = sysUserRepository.findByIdAndOrganization_Id(id, orgId)
                .orElseThrow(() -> new BusinessException(404, "user not found"));
        sysUserRepository.delete(user);
    }

    private AdminPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AdminPrincipal principal)) {
            throw new BusinessException(401, "unauthorized");
        }
        return principal;
    }
}
