package com.digitalsignage.admin.media.controller;

import com.digitalsignage.admin.common.api.ApiResponse;
import com.digitalsignage.admin.media.dto.ConfirmMediaRequest;
import com.digitalsignage.admin.media.dto.MediaResponse;
import com.digitalsignage.admin.media.dto.UploadPolicyRequest;
import com.digitalsignage.admin.media.dto.UploadPolicyResponse;
import com.digitalsignage.admin.media.service.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${app.api.admin-prefix}/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload-policy")
    public ApiResponse<UploadPolicyResponse> uploadPolicy(@Valid @RequestBody UploadPolicyRequest request) {
        return ApiResponse.ok(mediaService.uploadPolicy(request));
    }

    @PostMapping("/confirm")
    public ApiResponse<MediaResponse> confirm(@Valid @RequestBody ConfirmMediaRequest request) {
        return ApiResponse.ok(mediaService.confirm(request));
    }

    @GetMapping
    public ApiResponse<List<MediaResponse>> list() {
        return ApiResponse.ok(mediaService.listMedia());
    }

    @GetMapping("/{id}")
    public ApiResponse<MediaResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(mediaService.getMedia(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        mediaService.deleteMedia(id);
        return ApiResponse.ok();
    }
}
