package com.digitalsignage.admin.layout.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LayoutTemplateResponse {

    private String templateType;
    private String displayName;
}
