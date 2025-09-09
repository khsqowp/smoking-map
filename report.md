## 시스템 아키텍처 보고서

### 1. 프로젝트 개요

*   **프로젝트명:** Smoking_Map
*   **목적:** 흡연 구역 정보를 제공하고 관리하는 웹 애플리케이션으로 추정됩니다. (백엔드 API, 프론트엔드 웹 UI, 데이터베이스 연동)

### 2. 기술 스택

#### 2.1. 백엔드

*   **언어 및 버전:** Java 17
*   **프레임워크:** Spring Boot 3.2.5
*   **빌드 도구:** Gradle (버전은 `gradle-wrapper.properties`에 명시될 것으로 예상되나, 현재 파일에서는 3.2.5 플러그인 사용)
*   **주요 의존성:**
    *   `spring-boot-starter-data-jpa`: JPA (Java Persistence API)를 통한 데이터베이스 연동
    *   `spring-boot-starter-security`: Spring Security를 이용한 인증 및 권한 관리
    *   `spring-boot-starter-web`: RESTful API 및 웹 애플리케이션 개발
    *   `lombok`: Getter, Setter 등 보일러플레이트 코드 자동 생성
    *   `mysql-connector-j`: MySQL 데이터베이스 드라이버
    *   `spring-boot-devtools`: 개발 편의성 도구
    *   `spring-security-oauth2-client`: Google, Kakao 소셜 로그인 연동

#### 2.2. 프론트엔드

*   **프레임워크:** Next.js 14.2.3 (React 기반)
*   **언어:** TypeScript 5
*   **패키지 매니저:** npm (package-lock.json 존재)
*   **주요 의존성:**
    *   `@react-google-maps/api`: Google Maps API 연동
    *   `react-kakao-maps-sdk`: Kakao Map API 연동
    *   `axios`: HTTP 클라이언트 (백엔드 API 통신)
    *   `react-query`: 데이터 fetching 및 상태 관리
    *   `recoil`: 상태 관리 라이브러리
    *   `styled-components`: CSS-in-JS 스타일링
    *   `react-icons`: 아이콘 라이브러리
*   **개발 의존성:**
    *   `jest`, `@testing-library/react`, `@testing-library/jest-dom`: 단위 및 통합 테스트
    *   `cypress`: E2E (End-to-End) 테스트
    *   `eslint`, `eslint-config-next`: 코드 린팅
    *   `postcss`, `tailwindcss`: CSS 전처리기 및 유틸리티 우선 CSS 프레임워크

#### 2.3. 데이터베이스

*   **유형 및 버전:** MySQL 8.0

#### 2.4. 컨테이너화 및 오케스트레이션

*   **도구:** Docker, Docker Compose 3.8
*   **기반 이미지:** `openjdk:17-jdk-slim` (빌드), `openjdk:17-jre-slim` (런타임), `mysql:8.0`

### 3. 시스템 아키텍처 및 구조

*   **전체 구조:** 백엔드(Spring Boot)와 프론트엔드(Next.js)가 분리된 모놀리식 서비스 아키텍처를 따르며, Docker Compose를 통해 각 서비스를 컨테이너화하여 관리합니다. MySQL 데이터베이스는 별도의 서비스로 분리되어 있습니다.
*   **백엔드 구조:** Spring Boot 애플리케이션으로, `src/main/java` 아래에 비즈니스 로직, 컨트롤러, 서비스, 리포지토리 등이 구성될 것으로 예상됩니다. `src/main/resources/application.yml`을 통해 설정이 관리됩니다.
*   **프론트엔드 구조:** Next.js 애플리케이션으로, `smoking_map_frontend/src/app` 아래에 라우팅 및 페이지 컴포넌트가, `smoking_map_frontend/src/components` 아래에 재사용 가능한 UI 컴포넌트가, `smoking_map_frontend/src/utils` 아래에 유틸리티 함수 및 API 클라이언트가 구성될 것으로 예상됩니다.
*   **통신:** 프론트엔드는 `axios`를 사용하여 백엔드 API(`NEXT_PUBLIC_API_BASE_URL` 환경 변수로 설정된 URL)와 HTTP 통신을 수행합니다.

### 4. 설정

#### 4.1. 백엔드 (`application.yml`)

*   **서버 포트:** 8080
*   **데이터베이스 연결:**
    *   URL: `jdbc:mysql://localhost:3306/smoking_map?useSSL=false&allowPublicKeyRetrieval=true` (로컬 개발용)
    *   사용자명: `user`
    *   비밀번호: `password`
    *   드라이버: `com.mysql.cj.jdbc.Driver`
    *   JPA DDL 자동 생성: `update` (애플리케이션 시작 시 스키마 업데이트)
    *   SQL 로깅: `show-sql: true`, `format_sql: true`
*   **Spring Security OAuth2 클라이언트:**
    *   **Google:** `client-id`, `client-secret` (환경 변수 사용), `scope: email, profile`
    *   **Kakao:** `client-id`, `client-secret` (환경 변수 사용), `client-authentication-method: post`, `authorization-grant-type: authorization_code`, `redirect-uri: {baseUrl}/{action}/oauth2/code/{registrationId}`, `scope: profile_nickname, account_email`
    *   **Kakao Provider:** `authorization-uri`, `token-uri`, `user-info-uri`, `user-name-attribute: id`

#### 4.2. 프론트엔드

*   **Next.js 설정 (`next.config.mjs`):**
    *   `compiler.styledComponents: true`: Styled Components 사용 설정
    *   `images.domains`: `lh3.googleusercontent.com`, `k.kakaocdn.net` (외부 이미지 호스트 허용)
*   **TypeScript 설정 (`tsconfig.json`):**
    *   `target: es5`, `lib: dom, dom.iterable, esnext`
    *   `allowJs: true`, `skipLibCheck: true`, `strict: true`, `noEmit: true`, `esModuleInterop: true`
    *   `module: esnext`, `moduleResolution: bundler`, `resolveJsonModule: true`, `isolatedModules: true`, `jsx: preserve`, `incremental: true`
    *   `plugins: [{ name: "next" }]`
    *   `paths: { "@/*": ["./src/*"] }`: 절대 경로 임포트 설정

#### 4.3. Docker Compose (`docker-compose.yml`)

*   **서비스:** `backend`, `frontend`, `mysql`
*   **포트 매핑:**
    *   `backend`: `8080:8080`
    *   `frontend`: `3000:3000`
    *   `mysql`: `3306:3306`
*   **환경 변수:**
    *   `backend`: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_JPA_HIBERNATE_DDL_AUTO` (MySQL 서비스에 연결되도록 설정)
    *   `frontend`: `NEXT_PUBLIC_API_BASE_URL: http://localhost:8080` (백엔드 서비스 URL)
    *   `mysql`: `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`
*   **네트워크:** `smoking_map_network` (bridge 드라이버)를 사용하여 서비스 간 통신 격리
*   **볼륨:** `mysql_data` (MySQL 데이터 영속성 유지)

### 5. 기능 (현재 및 계획)

#### 5.1. 현재 (의존성 및 설정 기반 추정)

*   **사용자 인증:** Google 및 Kakao 소셜 로그인을 통한 인증
*   **웹 애플리케이션:** Next.js 기반의 프론트엔드 웹 페이지 제공
*   **데이터베이스 연동:** MySQL을 통한 데이터 저장 및 관리 (JPA 사용)
*   **지도 기능:** Google Maps 및 Kakao Map API를 활용한 지도 표시 및 상호작용

#### 5.2. 계획 (Todo.md 기반)

*   **백엔드:**
    *   사용자 등록 및 로그인 (소셜 로그인 외)
    *   흡연 구역 관리 API (CRUD)
    *   지도 서비스 API 연동 (위치 데이터)
    *   흡연 구역 검색 기능
    *   흡연 구역 리뷰/평점 시스템
    *   관리자 기능 (사용자 및 콘텐츠 관리)
    *   데이터베이스 쿼리 최적화
    *   단위 및 통합 테스트 추가
    *   캐싱 구현 (Redis 고려)
*   **프론트엔드:**
    *   흡연 구역 마커가 있는 메인 지도 뷰 구현
    *   사용자 인증 흐름 구현 (소셜 로그인 연동)
    *   흡연 구역 검색 및 필터링 UI
    *   흡연 구역 상세 정보 및 리뷰 표시 UI
    *   새로운 흡연 구역 제출 UI
    *   사용자 프로필 페이지
    *   반응형 디자인
    *   Google Maps / Kakao Map API 연동 (인터랙티브 지도)
    *   API 호출 오류 처리 및 로딩 상태
    *   접근성 개선 (A11y)
    *   Cypress를 이용한 E2E 테스트 작성
    *   이미지 로딩 및 전반적인 성능 최적화
*   **배포:**
    *   CI/CD 파이프라인 구축
    *   프로덕션 환경 변수 보안 설정
    *   모든 서비스에 HTTPS 적용
    *   리버스 프록시 (Nginx 등) 설정 (라우팅 및 SSL 종료)
    *   프로덕션 애플리케이션 상태 및 성능 모니터링
*   **일반:**
    *   프로젝트 구조 및 명명 규칙 개선
    *   포괄적인 문서화 (API 문서, 설정 가이드)
    *   보안 감사 및 발견 사항 처리
    *   성능 테스트

### 6. 보안

#### 6.1. 일반적인 보안 고려사항

*   **인증 및 권한:** Spring Security 및 OAuth2 클라이언트를 사용하여 사용자 인증 및 권한 부여를 처리합니다.
*   **민감 데이터:** 데이터베이스 자격 증명 및 OAuth2 클라이언트 시크릿과 같은 민감한 정보는 환경 변수를 통해 외부화됩니다.
*   **컨테이너 보안:** Dockerfile에서 멀티 스테이지 빌드를 사용하여 최종 이미지 크기를 줄이고 공격 표면을 최소화합니다.
*   **네트워크 격리:** Docker Compose 네트워크를 사용하여 서비스 간의 격리를 제공합니다.

#### 6.2. `SecurityReport.md` 기반 취약점/고려사항 및 권장 사항

*   **HTTPS 적용:** 프로덕션 환경에서는 프론트엔드와 백엔드 모두 HTTPS를 강제해야 합니다. (현재 HTTP 8080 포트 사용)
*   **환경 변수 보안:** 프로덕션 환경에서는 Docker secrets 또는 전용 비밀 관리 솔루션을 사용하여 환경 변수를 안전하게 관리해야 합니다.
*   **CORS 설정:** 백엔드에서 CORS 정책을 명시적으로 정의하고 제한해야 합니다.
*   **속도 제한 (Rate Limiting):** 무차별 대입 공격 또는 남용을 방지하기 위해 중요한 API 엔드포인트에 속도 제한을 구현해야 합니다.
*   **입력 유효성 검사:** 모든 사용자 제공 데이터에 대해 강력한 입력 유효성 검사를 수행하여 다양한 주입 공격(XSS, 명령 주입 등)을 방지해야 합니다.
*   **비루트 컨테이너:** Dockerfile을 구성하여 애플리케이션이 컨테이너 내에서 비루트 사용자로 실행되도록 해야 합니다.
*   **취약점 스캔:** Docker 이미지 및 프로젝트 의존성에 대해 알려진 취약점을 정기적으로 스캔해야 합니다.
*   **보안 헤더:** 프론트엔드에 적절한 보안 헤더(예: Content Security Policy, X-Frame-Options)를 구현해야 합니다.
*   **오류 처리:** 오류 메시지가 민감한 정보를 노출하지 않도록 해야 합니다.
*   **종속성 업데이트:** 모든 종속성(Spring Boot, Next.js, 라이브러리, Docker 이미지)을 최신 보안 버전으로 유지해야 합니다.
*   **코드 검토 및 침투 테스트:** 정기적인 코드 검토 및 침투 테스트를 통해 잠재적인 보안 취약점을 식별해야 합니다.

### 7. 배포

*   **로컬 개발:** `start.sh` 스크립트를 사용하여 백엔드(`gradlew bootRun`)와 프론트엔드(`npm run dev`)를 동시에 시작할 수 있습니다.
*   **컨테이너 기반 배포:** Docker 및 Docker Compose를 사용하여 애플리케이션을 컨테이너화하고 배포합니다.
*   **Dockerfile:**
    *   `Dockerfile` 및 `Dockerfile.backend`: 백엔드 애플리케이션을 위한 멀티 스테이지 빌드를 사용합니다. `openjdk:17-jdk-slim`으로 빌드하고 `openjdk:17-jre-slim`으로 런타임 이미지를 생성하여 이미지 크기를 최적화합니다. 8080 포트를 노출합니다.
    *   `Dockerfile.frontend`: 프론트엔드 애플리케이션을 위한 Dockerfile입니다. (내용은 제공되지 않았지만 `docker-compose.yml`에서 참조됨)