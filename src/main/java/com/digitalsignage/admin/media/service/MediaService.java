package com.digitalsignage.admin.media.service;

import com.digitalsignage.admin.media.dto.ConfirmMediaRequest;
import com.digitalsignage.admin.media.dto.MediaResponse;
import com.digitalsignage.admin.media.dto.UploadPolicyRequest;
import com.digitalsignage.admin.media.dto.UploadPolicyResponse;

import java.util.List;

public interface MediaService {

    UploadPolicyResponse uploadPolicy(UploadPolicyRequest request);

    MediaResponse confirm(ConfirmMediaRequest request);

    List<MediaResponse> listMedia();

    MediaResponse getMedia(Long id);

    void deleteMedia(Long id);
}
