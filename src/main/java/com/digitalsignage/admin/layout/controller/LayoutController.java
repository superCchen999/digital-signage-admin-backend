package com.digitalsignage.admin.layout.controller;

import com.digitalsignage.admin.common.api.ApiResponse;
import com.digitalsignage.admin.layout.dto.CreateLayoutRequest;
import com.digitalsignage.admin.layout.dto.LayoutResponse;
import com.digitalsignage.admin.layout.dto.LayoutTemplateResponse;
import com.digitalsignage.admin.layout.dto.UpdateLayoutRequest;
import com.digitalsignage.admin.layout.service.LayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${app.api.admin-prefix}")
@RequiredArgsConstructor
public class LayoutController {

    private final LayoutService layoutService;

    @GetMapping("/layout-templates")
    public ApiResponse<List<LayoutTemplateResponse>> listTemplates() {
        return ApiResponse.ok(layoutService.listTemplates());
    }

    @GetMapping("/layouts")
    public ApiResponse<List<LayoutResponse>> listLayouts() {
        return ApiResponse.ok(layoutService.listLayouts());
    }

    @GetMapping("/layouts/{id}")
    public ApiResponse<LayoutResponse> getLayout(@PathVariable Long id) {
        return ApiResponse.ok(layoutService.getLayout(id));
    }

    @PostMapping("/layouts")
    public ApiResponse<LayoutResponse> createLayout(@Valid @RequestBody CreateLayoutRequest request) {
        return ApiResponse.ok(layoutService.createLayout(request));
    }

    @PutMapping("/layouts/{id}")
    public ApiResponse<LayoutResponse> updateLayout(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLayoutRequest request) {
        return ApiResponse.ok(layoutService.updateLayout(id, request));
    }

    @DeleteMapping("/layouts/{id}")
    public ApiResponse<Void> deleteLayout(@PathVariable Long id) {
        layoutService.deleteLayout(id);
        return ApiResponse.ok();
    }
}
