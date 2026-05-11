package com.digitalsignage.admin.layout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LayoutRegionRequest {

    @NotBlank(message = "regionName is required")
    @Size(max = 128)
    private String regionName;

    @NotNull(message = "x is required")
    private Integer x;

    @NotNull(message = "y is required")
    private Integer y;

    @NotNull(message = "width is required")
    private Integer width;

    @NotNull(message = "height is required")
    private Integer height;

    @NotNull(message = "zIndex is required")
    private Integer zIndex;

    @NotBlank(message = "componentType is required")
    @Size(max = 64)
    private String componentType;

    @NotBlank(message = "configJson is required")
    private String configJson;
}
