package com.digitalsignage.admin.layout.dto;

import com.digitalsignage.admin.common.enums.LayoutStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateLayoutRequest {

    @NotBlank(message = "name is required")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "templateType is required")
    @Size(max = 64)
    private String templateType;

    @NotNull(message = "resolutionWidth is required")
    private Integer resolutionWidth;

    @NotNull(message = "resolutionHeight is required")
    private Integer resolutionHeight;

    @NotNull(message = "status is required")
    private LayoutStatus status;

    @NotEmpty(message = "regions is required")
    @Valid
    private List<LayoutRegionRequest> regions;
}
