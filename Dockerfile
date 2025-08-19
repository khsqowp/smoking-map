# 1. 베이스 이미지 선택 (M1/M2/M3 칩과 호환되는 ARM64 버전)
FROM amazoncorretto:17-al2023-headless

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 JAR 파일 복사 (파일명이 다르면 수정해야 합니다)
COPY build/libs/*.jar app.jar

# 4. 컨테이너 실행 시 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]