package com.digitalsignage.admin.layout.service.impl;

import com.digitalsignage.admin.common.exception.BusinessException;
import com.digitalsignage.admin.entity.Layout;
import com.digitalsignage.admin.entity.LayoutRegion;
import com.digitalsignage.admin.entity.Organization;
import com.digitalsignage.admin.layout.dto.CreateLayoutRequest;
import com.digitalsignage.admin.layout.dto.LayoutRegionRequest;
import com.digitalsignage.admin.layout.dto.LayoutRegionResponse;
import com.digitalsignage.admin.layout.dto.LayoutResponse;
import com.digitalsignage.admin.layout.dto.LayoutTemplateResponse;
import com.digitalsignage.admin.layout.dto.UpdateLayoutRequest;
import com.digitalsignage.admin.layout.repository.LayoutRegionRepository;
import com.digitalsignage.admin.layout.repository.LayoutRepository;
import com.digitalsignage.admin.layout.service.LayoutService;
import com.digitalsignage.admin.security.AdminPrincipal;
import com.digitalsignage.admin.user.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LayoutServiceImpl implements LayoutService {

    private static final List<LayoutTemplateResponse> TEMPLATE_RESPONSES = List.of(
            LayoutTemplateResponse.builder().templateType("SINGLE_FULL").displayName("Single Full").build(),
            LayoutTemplateResponse.builder().templateType("TOP_BOTTOM").displayName("Top Bottom").build(),
            LayoutTemplateResponse.builder().templateType("LEFT_RIGHT").displayName("Left Right").build(),
            LayoutTemplateResponse.builder().templateType("MAIN_SIDEBAR").displayName("Main Sidebar").build()
    );

    private final LayoutRepository layoutRepository;
    private final LayoutRegionRepository layoutRegionRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LayoutTemplateResponse> listTemplates() {
        return TEMPLATE_RESPONSES;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LayoutResponse> listLayouts() {
        Long organizationId = currentPrincipal().getOrganizationId();
        return layoutRepository.findByOrganization_IdOrderByUpdatedAtDesc(organizationId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LayoutResponse getLayout(Long id) {
        Long organizationId = currentPrincipal().getOrganizationId();
        Layout layout = layoutRepository.findByIdAndOrganization_Id(id, organizationId)
                .orElseThrow(() -> new BusinessException(404, "layout not found"));
        return toResponse(layout);
    }

    @Override
    @Transactional
    public LayoutResponse createLayout(CreateLayoutRequest request) {
        validateRegions(request.getRegions());

        Long organizationId = currentPrincipal().getOrganizationId();
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException(400, "organization not found"));

        Layout layout = new Layout();
        layout.setOrganization(organization);
        layout.setName(request.getName());
        layout.setTemplateType(request.getTemplateType());
        layout.setResolutionWidth(request.getResolutionWidth());
        layout.setResolutionHeight(request.getResolutionHeight());
        layout.setStatus(request.getStatus());

        Layout savedLayout = layoutRepository.save(layout);
        saveRegions(savedLayout, request.getRegions());

        Layout foundLayout = layoutRepository.findByIdAndOrganization_Id(savedLayout.getId(), organizationId)
                .orElseThrow(() -> new BusinessException(404, "layout not found"));
        return toResponse(foundLayout);
    }

    @Override
    @Transactional
    public LayoutResponse updateLayout(Long id, UpdateLayoutRequest request) {
        validateRegions(request.getRegions());

        Long organizationId = currentPrincipal().getOrganizationId();
        Layout layout = layoutRepository.findByIdAndOrganization_Id(id, organizationId)
                .orElseThrow(() -> new BusinessException(404, "layout not found"));

        if (StringUtils.hasText(request.getName())) {
            layout.setName(request.getName());
        }
        if (StringUtils.hasText(request.getTemplateType())) {
            layout.setTemplateType(request.getTemplateType());
        }
        if (request.getResolutionWidth() != null) {
            layout.setResolutionWidth(request.getResolutionWidth());
        }
        if (request.getResolutionHeight() != null) {
            layout.setResolutionHeight(request.getResolutionHeight());
        }
        if (request.getStatus() != null) {
            layout.setStatus(request.getStatus());
        }

        Layout savedLayout = layoutRepository.save(layout);
        layoutRegionRepository.deleteByLayout_Id(savedLayout.getId());
        saveRegions(savedLayout, request.getRegions());

        Layout foundLayout = layoutRepository.findByIdAndOrganization_Id(savedLayout.getId(), organizationId)
                .orElseThrow(() -> new BusinessException(404, "layout not found"));
        return toResponse(foundLayout);
    }

    @Override
    @Transactional
    public void deleteLayout(Long id) {
        Long organizationId = currentPrincipal().getOrganizationId();
        Layout layout = layoutRepository.findByIdAndOrganization_Id(id, organizationId)
                .orElseThrow(() -> new BusinessException(404, "layout not found"));
        layoutRegionRepository.deleteByLayout_Id(layout.getId());
        layoutRepository.delete(layout);
    }

    private LayoutResponse toResponse(Layout layout) {
        List<LayoutRegionResponse> regions = layoutRegionRepository.findByLayoutIdOrderBySort(layout.getId())
                .stream()
                .map(LayoutRegionResponse::fromEntity)
                .toList();
        return LayoutResponse.fromEntity(layout, regions);
    }

    private void saveRegions(Layout layout, List<LayoutRegionRequest> regionRequests) {
        for (LayoutRegionRequest request : regionRequests) {
            LayoutRegion region = new LayoutRegion();
            region.setLayout(layout);
            region.setRegionName(request.getRegionName());
            region.setX(request.getX());
            region.setY(request.getY());
            region.setWidth(request.getWidth());
            region.setHeight(request.getHeight());
            region.setZIndex(request.getZIndex());
            region.setComponentType(request.getComponentType());
            region.setConfigJson(request.getConfigJson());
            layoutRegionRepository.save(region);
        }
    }

    private void validateRegions(List<LayoutRegionRequest> regions) {
        if (regions == null || regions.isEmpty()) {
            throw new BusinessException(400, "regions is required");
        }
    }

    private AdminPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AdminPrincipal principal)) {
            throw new BusinessException(401, "unauthorized");
        }
        return principal;
    }
}
