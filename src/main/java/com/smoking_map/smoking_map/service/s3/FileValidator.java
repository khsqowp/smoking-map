package com.smoking_map.smoking_map.service.s3;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Component
public class FileValidator {

    // 허용할 MIME 타입 목록
    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/bmp",
            "image/webp"
    );

    // 허용할 파일 확장자 목록
    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    public void validateImageFile(MultipartFile file) {
        // 1. 파일 존재 여부 및 비어있는지 확인
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 없습니다.");
        }

        String mimeType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);

        // 2. 파일 이름 및 확장자 존재 여부 확인
        if (originalFilename == null || extension == null) {
            throw new IllegalArgumentException("파일 이름이나 확장자가 유효하지 않습니다.");
        }

        // 3. MIME 타입 검증
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (MIME 타입: " + mimeType + ")");
        }

        // 4. 확장자 검증
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다. (확장자: " + extension + ")");
        }
    }
}