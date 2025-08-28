package com.smoking_map.smoking_map.service.s3;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    private static final int TARGET_WIDTH = 1200;

    // --- ▼▼▼ [수정] 기존 메서드는 새로운 오버로딩 메서드를 호출하도록 변경 ▼▼▼ ---
    public String upload(MultipartFile multipartFile, String fullPath) throws IOException {
        // MultipartFile에서 InputStream을 추출하여 새로운 upload 메서드에 전달
        return upload(multipartFile.getInputStream(), fullPath, multipartFile.getOriginalFilename());
    }
    // --- ▲▲▲ [수정] 기존 메서드는 새로운 오버로딩 메서드를 호출하도록 변경 ▲▲▲ ---

    // --- ▼▼▼ [수정] InputStream을 직접 처리하는 새로운 upload 메서드 추가 ▼▼▼ ---
    public String upload(InputStream inputStream, String fullPath, String originalFilename) throws IOException {
        InputStream resizedInputStream;
        String extension = StringUtils.getFilenameExtension(originalFilename);

        BufferedImage originalImage = ImageIO.read(inputStream);
        if (originalImage == null) {
            throw new IOException("유효하지 않은 이미지 파일입니다.");
        }

        if (originalImage.getWidth() > TARGET_WIDTH) {
            BufferedImage resizedImage = Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_WIDTH, TARGET_WIDTH);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 확장자가 null일 경우를 대비하여 "jpg"를 기본값으로 사용
            ImageIO.write(resizedImage, extension != null ? extension : "jpg", baos);
            resizedInputStream = new ByteArrayInputStream(baos.toByteArray());
        } else {
            // 이미지가 리사이즈되지 않은 경우, 원본 이미지를 다시 InputStream으로 만들어야 합니다.
            // ByteArrayOutputStream을 사용하여 BufferedImage를 InputStream으로 변환합니다.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(originalImage, extension != null ? extension : "jpg", baos);
            resizedInputStream = new ByteArrayInputStream(baos.toByteArray());
        }

        log.info("Uploading file to S3: bucket={}, path={}", bucket, fullPath);
        return s3Template.upload(bucket, fullPath, resizedInputStream).getURL().toString();
    }
    // --- ▲▲▲ [수정] InputStream을 직접 처리하는 새로운 upload 메서드 추가 ▲▲▲ ---

    public void delete(String fileUrl) {
        try {
            String urlWithoutHost = fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1);
            String key = URLDecoder.decode(urlWithoutHost, StandardCharsets.UTF_8);

            s3Template.deleteObject(bucket, key);
            log.info("Successfully deleted file from S3: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: url={}", fileUrl, e);
        }
    }
}