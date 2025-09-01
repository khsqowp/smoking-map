package com.smoking_map.smoking_map.service.s3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileValidator 단위 테스트")
class FileValidatorTest {

    private final FileValidator fileValidator = new FileValidator();

    @Nested
    @DisplayName("validateImageFile (이미지 파일 검증) 테스트")
    class Describe_validateImageFile {

        @ParameterizedTest
        @ValueSource(strings = {"image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"})
        @DisplayName("허용된 MIME 타입의 이미지 파일은, 검증을 통과한다")
        void 성공_허용된_MIME_타입(String mimeType) {
            // 준비 (Arrange)
            // 왜? 시스템이 허용하는 모든 종류의 이미지 파일 형식을 정상적으로 처리하는지 검증하기 위함.
            String extension = mimeType.split("/")[1];
            MockMultipartFile file = new MockMultipartFile(
                    "image",
                    "test_image." + extension,
                    mimeType,
                    "image content".getBytes()
            );

            // 실행 및 검증 (Act & Assert)
            // 예외가 발생하지 않으면 테스트 성공
            assertThatCode(() -> fileValidator.validateImageFile(file)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("파일이 null이거나 비어있으면, IllegalArgumentException이 발생한다")
        void 실패_파일이_없거나_비어있음() {
            // 준비 (Arrange)
            // 왜? 파일이 없는 비정상적인 요청을 올바르게 차단하는지 검증하기 위함.
            MockMultipartFile emptyFile = new MockMultipartFile("image", "", "image/jpeg", new byte[0]);

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> fileValidator.validateImageFile(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("업로드된 파일이 없습니다.");

            assertThatThrownBy(() -> fileValidator.validateImageFile(emptyFile))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("업로드된 파일이 없습니다.");
        }

        @Test
        @DisplayName("파일 이름이나 확장자가 없으면, IllegalArgumentException이 발생한다")
        void 실패_파일이름이나_확장자_없음() {
            // 준비 (Arrange)
            // 왜? 파일 정보가 불완전한 비정상적인 케이스를 올바르게 처리하는지 검증하기 위함.
            MockMultipartFile noFilename = new MockMultipartFile("image", null, "image/jpeg", "content".getBytes());
            MockMultipartFile noExtension = new MockMultipartFile("image", "test", "image/jpeg", "content".getBytes());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> fileValidator.validateImageFile(noFilename))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("파일 이름이나 확장자가 유효하지 않습니다.");

            assertThatThrownBy(() -> fileValidator.validateImageFile(noExtension))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("파일 이름이나 확장자가 유효하지 않습니다.");
        }

        @Test
        @DisplayName("허용되지 않은 MIME 타입이면, IllegalArgumentException이 발생한다")
        void 실패_허용되지_않은_MIME_타입() {
            // 준비 (Arrange)
            // 왜? 이미지 파일이 아닌 다른 종류의 파일(예: text, pdf) 업로드를 차단하는 보안 로직을 검증하기 위함.
            MockMultipartFile textFile = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> fileValidator.validateImageFile(textFile))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("허용되지 않는 파일 형식입니다.");
        }

        @Test
        @DisplayName("허용되지 않은 확장자이면, IllegalArgumentException이 발생한다")
        void 실패_허용되지_않은_확장자() {
            // 준비 (Arrange)
            // 왜? MIME 타입은 이미지이지만 확장자가 다른 경우(예: .exe)의 비정상적인 케이스를 차단하는지 검증하기 위함.
            MockMultipartFile exeFile = new MockMultipartFile("file", "image.exe", "image/jpeg", "content".getBytes());

            // 실행 및 검증 (Act & Assert)
            assertThatThrownBy(() -> fileValidator.validateImageFile(exeFile))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("허용되지 않는 파일 확장자입니다.");
        }
    }
}
