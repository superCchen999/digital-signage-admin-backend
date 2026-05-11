package com.digitalsignage.admin.layout.dto;

import com.digitalsignage.admin.common.enums.LayoutStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateLayoutRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 64)
    private String templateType;

    private Integer resolutionWidth;

    private Integer resolutionHeight;

    private LayoutStatus status;

    @NotNull(message = "regions is required")
    @Valid
    private List<LayoutRegionRequest> regions;
}
