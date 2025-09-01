package com.smoking_map.smoking_map.service.s3;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3Uploader 단위 테스트")
class S3UploaderTest {

    @InjectMocks
    private S3Uploader s3Uploader;

    @Mock
    private S3Template s3Template;

    @Mock
    private S3Resource s3Resource;

    private final String BUCKET_NAME = "test-bucket";

    @BeforeEach
    void setUp() {
        // @Value로 주입되는 bucket 값을 테스트에서 수동으로 설정
        ReflectionTestUtils.setField(s3Uploader, "bucket", BUCKET_NAME);
    }

    // 테스트용 이미지 InputStream을 생성하는 헬퍼 메서드
    private InputStream createImageInputStream(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @Nested
    @DisplayName("upload (파일 업로드) 테스트")
    class Describe_upload {

        @Test
        @DisplayName("이미지 너비가 1200px보다 크면, 리사이징하여 업로드한다")
        void 성공_이미지_리사이징() throws IOException {
            // 준비 (Arrange)
            // 왜? 큰 이미지 파일이 업로드될 때, 시스템이 자동으로 이미지 크기를 조절하여 저장 공간과 트래픽을 최적화하는지 검증하기 위함.
            InputStream wideImageInputStream = createImageInputStream(2000, 1000);
            given(s3Template.upload(anyString(), anyString(), any())).willReturn(s3Resource);
            given(s3Resource.getURL()).willReturn(new URL("http://s3.com/resized_image.jpg"));

            // 실행 (Act)
            String uploadedUrl = s3Uploader.upload(wideImageInputStream, "path/image.jpg", "image.jpg");

            // 검증 (Assert)
            assertThat(uploadedUrl).isEqualTo("http://s3.com/resized_image.jpg");
            verify(s3Template).upload(anyString(), anyString(), any(InputStream.class));
        }

        @Test
        @DisplayName("이미지 너비가 1200px 이하면, 원본 그대로 업로드한다")
        void 성공_이미지_리사이징_안함() throws IOException {
            // 준비 (Arrange)
            // 왜? 작은 이미지 파일은 불필요한 리사이징 과정을 거치지 않고 원본 그대로 업로드되는지 검증하기 위함.
            InputStream narrowImageInputStream = createImageInputStream(800, 600);
            given(s3Template.upload(anyString(), anyString(), any())).willReturn(s3Resource);
            given(s3Resource.getURL()).willReturn(new URL("http://s3.com/original_image.jpg"));

            // 실행 (Act)
            String uploadedUrl = s3Uploader.upload(narrowImageInputStream, "path/image.jpg", "image.jpg");

            // 검증 (Assert)
            assertThat(uploadedUrl).isEqualTo("http://s3.com/original_image.jpg");
            verify(s3Template).upload(anyString(), anyString(), any(InputStream.class));
        }

        @Test
        @DisplayName("유효하지 않은 이미지 스트림이면, IOException이 발생한다")
        void 실패_잘못된_이미지_스트림() {
            // 준비 (Arrange)
            // 왜? 이미지 파일로 위장한 다른 데이터가 업로드되는 것을 방지하는지, 이미지 처리 라이브러리의 예외를 올바르게 처리하는지 검증하기 위함.
            InputStream invalidInputStream = new ByteArrayInputStream("not an image".getBytes());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> s3Uploader.upload(invalidInputStream, "path/image.jpg", "image.jpg"))
                    .isInstanceOf(IOException.class)
                    .hasMessage("유효하지 않은 이미지 파일입니다.");
        }
    }

    @Nested
    @DisplayName("delete (파일 삭제) 테스트")
    class Describe_delete {

        @Test
        @DisplayName("정상적인 S3 URL을 받으면, URL을 파싱하여 올바른 키로 삭제를 요청한다")
        void 성공_파일_삭제() {
            // 준비 (Arrange)
            // 왜? S3에 저장된 파일을 URL을 통해 정확하게 삭제하는 핵심 기능이 정상 동작하는지 검증하기 위함.
            String fileUrl = "https://s3.ap-northeast-2.amazonaws.com/" + BUCKET_NAME + "/places/서울시/image.jpg";
            String expectedKey = "places/서울시/image.jpg";

            // 실행 (Act)
            s3Uploader.delete(fileUrl);

            // 검증 (Assert)
            verify(s3Template).deleteObject(BUCKET_NAME, expectedKey);
        }

        @Test
        @DisplayName("URL에 인코딩된 문자가 포함되어 있으면, 디코딩하여 올바른 키로 삭제를 요청한다")
        void 성공_인코딩된_URL_처리() {
            // 준비 (Arrange)
            // 왜? 파일명에 공백이나 한글 등 특수문자가 포함되어 URL 인코딩된 경우에도 정상적으로 파일을 찾아 삭제하는지 검증하기 위함.
            String fileUrl = "https://s3.ap-northeast-2.amazonaws.com/" + BUCKET_NAME + "/places/%EC%84%9C%EC%9A%B8%EC%8B%9C/image%201.jpg";
            String expectedKey = "places/서울시/image 1.jpg";

            // 실행 (Act)
            s3Uploader.delete(fileUrl);

            // 검증 (Assert)
            verify(s3Template).deleteObject(BUCKET_NAME, expectedKey);
        }

        @Test
        @DisplayName("S3 삭제 중 예외가 발생해도, 서비스는 중단되지 않고 로그만 남긴다")
        void 성공_S3_삭제_예외_처리() {
            // 준비 (Arrange)
            // 왜? 외부 시스템(S3)의 일시적인 오류가 전체 서비스의 장애로 이어지지 않도록 예외 처리가 견고하게 되어 있는지 검증하기 위함.
            String fileUrl = "https://s3.ap-northeast-2.amazonaws.com/" + BUCKET_NAME + "/places/image.jpg";
            doThrow(new RuntimeException("S3 is down")).when(s3Template).deleteObject(anyString(), anyString());

            // 실행 및 검증 (Act & Assert)
            // 예외가 전파되지 않는 것을 확인
            assertThatCode(() -> s3Uploader.delete(fileUrl)).doesNotThrowAnyException();
        }
    }
}
