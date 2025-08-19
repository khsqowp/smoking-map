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

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    private static final int TARGET_WIDTH = 1200;

    public String upload(MultipartFile multipartFile, String fullPath) throws IOException {
        InputStream resizedInputStream;
        String extension = StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());

        BufferedImage originalImage = ImageIO.read(multipartFile.getInputStream());
        if (originalImage == null) {
            throw new IOException("유효하지 않은 이미지 파일입니다.");
        }

        if (originalImage.getWidth() > TARGET_WIDTH) {
            BufferedImage resizedImage = Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_WIDTH, TARGET_WIDTH);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, extension, baos);
            resizedInputStream = new ByteArrayInputStream(baos.toByteArray());
        } else {
            resizedInputStream = multipartFile.getInputStream();
        }

        log.info("Uploading file to S3: bucket={}, path={}", bucket, fullPath);
        return s3Template.upload(bucket, fullPath, resizedInputStream).getURL().toString();
    }
}