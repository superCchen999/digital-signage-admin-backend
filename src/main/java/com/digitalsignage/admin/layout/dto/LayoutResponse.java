package com.digitalsignage.admin.layout.dto;

import com.digitalsignage.admin.common.enums.LayoutStatus;
import com.digitalsignage.admin.entity.Layout;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class LayoutResponse {

    private Long id;
    private Long organizationId;
    private String name;
    private String templateType;
    private Integer resolutionWidth;
    private Integer resolutionHeight;
    private LayoutStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LayoutRegionResponse> regions;

    public static LayoutResponse fromEntity(Layout layout, List<LayoutRegionResponse> regions) {
        return LayoutResponse.builder()
                .id(layout.getId())
                .organizationId(layout.getOrganization().getId())
                .name(layout.getName())
                .templateType(layout.getTemplateType())
                .resolutionWidth(layout.getResolutionWidth())
                .resolutionHeight(layout.getResolutionHeight())
                .status(layout.getStatus())
                .createdAt(layout.getCreatedAt())
                .updatedAt(layout.getUpdatedAt())
                .regions(regions)
                .build();
    }
}
