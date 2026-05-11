package com.digitalsignage.admin.layout.dto;

import com.digitalsignage.admin.entity.LayoutRegion;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LayoutRegionResponse {

    private Long id;
    private String regionName;
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    private Integer zIndex;
    private String componentType;
    private String configJson;

    public static LayoutRegionResponse fromEntity(LayoutRegion region) {
        return LayoutRegionResponse.builder()
                .id(region.getId())
                .regionName(region.getRegionName())
                .x(region.getX())
                .y(region.getY())
                .width(region.getWidth())
                .height(region.getHeight())
                .zIndex(region.getZIndex())
                .componentType(region.getComponentType())
                .configJson(region.getConfigJson())
                .build();
    }
}
