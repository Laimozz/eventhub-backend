package com.eventhub.backend.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record UploadAvatarRequest(
        Integer userId,
        MultipartFile file
) {}
