# Smoking Map: 내 주변 흡연구역 정보 서비스

[![Live Demo](https://img.shields.io/badge/Live_Demo-Visit_Now-blue?style=for-the-badge&logo=vercel)](https://smokingmap.duckdns.org/)

사용자가 직접 흡연구역을 제보하고, 리뷰를 통해 정보를 공유하며 함께 지도를 완성해나가는 위치 기반 웹 서비스입니다.

![Smoking Map Demo](./smoking_map_frontend/public/smoking_map_demo.gif)
*(데모 GIF 예시입니다. 실제 작동 모습을 녹화하여 교체하시는 것을 추천합니다.)*

---

## 🧐 프로젝트 개요

흡연자들은 종종 지정된 흡연구역을 찾기 위해 많은 시간을 헤매곤 합니다. **Smoking Map**은 이러한 불편함을 해소하고자 시작된 프로젝트입니다. 단순히 흡연구역 위치를 알려주는 것을 넘어, 사용자들의 집단지성(리뷰, 평점, 사진 정보)을 통해 정보의 질을 높이고, 즐겨찾기 등 개인화 기능을 더해 사용자 경험을 개선하는 것을 목표로 합니다.

이 프로젝트를 통해 풀스택 개발 역량을 종합적으로 보여주고자 했습니다. **Docker**를 이용한 컨테이너 기반의 서비스 구축, **Nginx**를 활용한 리버스 프록시 및 HTTPS 적용, **Spring Boot** 기반의 안정적인 백엔드 API 서버, **Next.js**를 활용한 동적인 프론트엔드 구현 등 최신 웹 개발 트렌드를 적극적으로 반영하였습니다.

## 🛠️ 기술 스택 및 아키텍처

### Tech Stack

| 구분 | 기술 |
| --- | --- |
| **Frontend** | `Next.js`, `React`, `TypeScript`, `Naver Maps API` |
| **Backend** | `Java 17`, `Spring Boot`, `Spring Security`, `Spring Data JPA` |
| **Database** | `MySQL`, `H2` (테스트용) |
| **Infra/DevOps** | `Docker`, `Docker Compose`, `Nginx`, `AWS S3` |

### Architecture

![Architecture Diagram](./smoking_map_frontend/public/architecture.png)
*(아키텍처 다이어그램 예시입니다. 실제 구조에 맞게 다이어그램을 만들어 교체하세요.)*

1.  **Nginx (Reverse Proxy)**: 모든 외부 요청(HTTP/HTTPS)을 수신하여 각 서비스에 맞게 라우팅합니다. HTTPS 암호화, 로드 밸런싱, 정적 파일 캐싱을 담당하여 시스템의 안정성과 성능을 향상시킵니다.
2.  **Frontend (Next.js)**: 사용자가 직접 상호작용하는 UI를 제공합니다. Naver 지도 위에 흡연구역 정보를 시각화하고, 백엔드 API와 통신하여 데이터를 동적으로 표시합니다.
3.  **Backend (Spring Boot)**: 비즈니스 로직을 처리하는 RESTful API 서버입니다. 사용자 인증(OAuth2), 데이터베이스 CRUD, AWS S3 파일 업로드 등 핵심 기능을 담당합니다.
4.  **Database (MySQL)**: 사용자, 흡연구역, 리뷰 등 서비스의 모든 데이터를 영구적으로 저장합니다.
5.  **AWS S3**: 사용자가 업로드하는 흡연구역 이미지 등 대용량 정적 파일을 저장하는 비용 효율적이고 확장성 있는 스토리지입니다.

---

## ✨ 주요 기능

### 사용자 기능
- **🗺️ 지도 기반 흡연구역 조회**: Naver Maps API를 연동하여 지도 위에서 직관적으로 흡연구역 위치를 확인할 수 있습니다.
- **⭐ 리뷰 및 평점**: 각 장소에 별점과 한줄평을 남겨 다른 사용자들과 유용한 정보를 공유할 수 있습니다.
- **❤️ 즐겨찾기**: 자주 방문하는 장소를 즐겨찾기에 추가하여 손쉽게 다시 찾아볼 수 있습니다.
- **📍 신규 장소 제보**: 지도에 없는 새로운 흡연구역을 직접 등록하여 지도 완성에 기여할 수 있습니다.
- **🔐 소셜 로그인**: Google OAuth2를 이용해 사용자가 안전하고 간편하게 로그인할 수 있습니다.
- **📢 공지사항 팝업**: 서비스의 중요 업데이트나 이벤트를 사용자에게 효과적으로 전달합니다.

### 관리자 기능
- **📊 대시보드**: 서비스 현황을 한눈에 파악할 수 있는 통계 및 대시보드를 제공합니다.
- **🛠️ 콘텐츠 관리**: 사용자가 제보한 장소, 리뷰, 신고 내역 등을 관리하며 서비스의 품질을 유지합니다.
- **👥 사용자 관리**: 사용자 계정 및 권한을 관리할 수 있습니다.

---

## 🚀 로컬에서 실행하기

프로젝트를 로컬 환경에서 실행하려면 아래의 가이드를 따라주세요.

### 사전 요구사항
- Docker
- Docker Compose

### 실행 방법
1.  **프로젝트 클론**
    ```bash
    git clone https://github.com/your-username/Smoking_Map.git
    cd Smoking_Map
    ```

2.  **환경 변수 설정**
    프로젝트 루트 디렉토리에 `.env` 파일을 생성하고, 아래 내용을 참고하여 실제 값으로 채워주세요. `docker-compose.yml` 파일에서 필요한 환경 변수들을 확인할 수 있습니다.

    ```env
    # .env
    
    # Database
    DB_URL=jdbc:mysql://mysql:3306/smoking_map?useSSL=false&allowPublicKeyRetrieval=true
    DB_USERNAME=your_db_user
    DB_PASSWORD=your_db_password

    # JWT Secret Key
    JWT_SECRET=your_jwt_secret_key

    # OAuth2 - Google
    GOOGLE_CLIENT_ID=your_google_client_id
    GOOGLE_CLIENT_SECRET=your_google_client_secret

    # AWS S3
    AWS_ACCESS_KEY=your_aws_access_key
    AWS_SECRET_KEY=your_aws_secret_key
    AWS_S3_BUCKET=your_s3_bucket_name
    ```
    *Frontend의 네이버 지도 클라이언트 ID는 `docker-compose.yml` 파일의 `frontend.build.args` 부분에서 직접 설정해야 합니다.*

3.  **Docker Compose 실행**
    ```bash
    docker-compose up --build -d
    ```

4.  **서비스 접속**
    -   **Frontend:** `http://localhost:3001`
    -   **Backend API:** `http://localhost:8080`

---

## 🗓️ 향후 개선 계획

- **현재 위치 기반 지도 이동**: 앱 실행 시 사용자의 현재 위치를 중심으로 지도를 자동 이동하여 사용 편의성을 증대시킬 계획입니다.
- **로그인 세션 유지 시간 연장**: 사용자가 더 오랫동안 로그인 상태를 유지할 수 있도록 세션 관리 정책을 개선할 예정입니다.
- **관리자 대시보드 통계 고도화**: 기존의 주간/월간 통계에 더해 일간 통계 등 다양한 기준의 분석 데이터를 추가할 계획입니다.
- **수익화 모델 도입**: 지역 소상공인 광고, 장소 스폰서십 등 서비스의 지속 가능성을 위한 다양한 수익 모델을 검토하고 있습니다.
