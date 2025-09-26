# 📚 API 문서화 예시 - Smoking Map

## 🎯 API 문서화란?

현재 프로젝트의 모든 REST API를 **자동으로** 문서화하여 대화형 웹페이지로 제공하는 것입니다.

---

## 📋 현재 API 현황 (9개 컨트롤러)

### 1. 📍 **PlaceApiController** - 장소 관리
```http
POST   /api/v1/places                    # 장소 등록 (이미지 포함)
GET    /api/v1/places                    # 전체 장소 조회
GET    /api/v1/places/search             # 장소 검색
GET    /api/v1/places/{id}               # 장소 상세 조회
POST   /api/v1/places/{id}/view          # 조회수 증가
```

### 2. ⭐ **ReviewApiController** - 리뷰 관리  
```http
POST   /api/v1/reviews                   # 리뷰 작성
GET    /api/v1/places/{id}/reviews       # 특정 장소 리뷰 조회
DELETE /api/v1/reviews/{id}              # 리뷰 삭제
```

### 3. 👤 **FavoriteApiController** - 즐겨찾기
```http
POST   /api/v1/favorites                 # 즐겨찾기 추가
GET    /api/v1/favorites                 # 내 즐겨찾기 조회
DELETE /api/v1/favorites/{id}            # 즐겨찾기 삭제
```

### 4. 📊 **AdminApiController** - 관리자 기능
```http
GET    /api/v1/admin/dashboard           # 대시보드 데이터
GET    /api/v1/admin/places              # 관리자용 장소 목록
GET    /api/v1/admin/places/{id}         # 관리자용 장소 상세
```

### 5. 📢 **AnnouncementApiController** - 공지사항
```http
GET    /api/v1/announcements            # 공지사항 조회
POST   /api/v1/admin/announcements      # 공지사항 작성 (관리자)
```

### 6. 🔧 **EditRequestApiController** - 수정 요청
```http
POST   /api/v1/edit-requests            # 장소 수정 요청
GET    /api/v1/admin/edit-requests      # 수정 요청 관리 (관리자)
```

### 7. 📊 **UserActivityLogApiController** - 사용자 활동 로그
```http
POST   /api/v1/activity-logs            # 활동 로그 기록
```

### 8. 🚨 **ReportApiController** - 신고 관리
```http
POST   /api/v1/reports                  # 신고 접수
GET    /api/v1/admin/reports            # 신고 관리 (관리자)
```

### 9. 🏠 **IndexController** - 메인 페이지
```http
GET    /                               # 메인 페이지
```

---

## 🔧 Swagger 적용 후 모습

### **1. 자동 생성되는 API 문서 웹페이지**
```
URL: http://localhost:8080/swagger-ui.html
```

### **2. 실제 화면 예시**
```
┌─────────────────────────────────────────────────┐
│  🌐 Smoking Map API Documentation v1.0          │
├─────────────────────────────────────────────────┤
│                                                 │
│  📍 place-api-controller                        │
│  ├─ POST /api/v1/places                        │
│  │   ├─ 📝 장소를 새로 등록합니다                │
│  │   ├─ 📥 Parameters:                          │
│  │   │   ├─ requestDto: PlaceSaveRequestDto     │
│  │   │   └─ images: MultipartFile[]             │
│  │   ├─ 📤 Response: Long                       │
│  │   └─ [🧪 Try it out] ← 클릭하면 바로 테스트!    │
│  │                                              │
│  ├─ GET /api/v1/places                         │
│  │   ├─ 📝 모든 장소 목록을 조회합니다             │
│  │   ├─ 📤 Response: List<PlaceResponseDto>      │
│  │   └─ [🧪 Try it out]                        │
│  │                                              │
│  └─ GET /api/v1/places/search                  │
│      ├─ 📝 키워드로 장소를 검색합니다             │
│      ├─ 📥 Parameters: keyword (string)         │
│      └─ [🧪 Try it out]                        │
│                                                 │
│  ⭐ review-api-controller                       │
│  ├─ POST /api/v1/reviews                       │
│  └─ GET /api/v1/places/{id}/reviews            │
│                                                 │
│  👤 favorite-api-controller                     │
│  📊 admin-api-controller                        │
│  📢 announcement-api-controller                 │
└─────────────────────────────────────────────────┘
```

### **3. "Try it out" 기능**
```
┌─────────────────────────────────────────────────┐
│  POST /api/v1/places                           │
├─────────────────────────────────────────────────┤
│  Parameters:                                    │
│                                                 │
│  requestDto: ┌─────────────────────────────────┐ │
│             │ {                               │ │
│             │   "name": "강남역 흡연구역",      │ │
│             │   "address": "서울 강남구...",   │ │
│             │   "latitude": 37.498095,        │ │
│             │   "longitude": 127.02761        │ │
│             │ }                               │ │
│             └─────────────────────────────────┘ │
│                                                 │
│  images: [파일 선택] [파일 선택]                  │
│                                                 │
│  [🚀 Execute] ← 클릭하면 실제 API 호출!          │
└─────────────────────────────────────────────────┘
```

### **4. 응답 결과 실시간 표시**
```
┌─────────────────────────────────────────────────┐
│  📤 Response                                    │
├─────────────────────────────────────────────────┤
│  Code: 200 ✅                                   │
│  Response body: 1234                            │
│                                                 │
│  📋 Response headers:                           │
│  content-type: application/json                 │
│  date: Thu, 19 Dec 2024 12:00:00 GMT          │
└─────────────────────────────────────────────────┘
```

---

## 🎯 API 문서화의 장점

### **개발자 측면**
- ✅ 모든 API를 한 눈에 파악 가능
- ✅ 파라미터 형식을 즉시 확인
- ✅ 브라우저에서 바로 API 테스트 가능
- ✅ Postman 없이도 개발 가능

### **팀 협업 측면**  
- ✅ 프론트엔드 ↔ 백엔드 소통 원활
- ✅ 신입 개발자 온보딩 시간 단축
- ✅ API 변경사항 자동 반영
- ✅ 외부 개발자와의 협업 용이

### **운영 측면**
- ✅ API 스펙 문서 별도 관리 불필요
- ✅ 버전 관리 자동화
- ✅ 테스트 환경 구축 시간 단축

---

## 🔧 구현 방법

### **1. build.gradle에 의존성 추가**
```gradle
dependencies {
    // Swagger 3 (OpenAPI 3)
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
}
```

### **2. Configuration 클래스 생성**
```java
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI smokingMapAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Smoking Map API")
                .version("v1.0")
                .description("흡연 지도 서비스 API 문서"));
    }
}
```

### **3. 컨트롤러에 어노테이션 추가**
```java
@RestController
@Tag(name = "장소 API", description = "흡연 장소 관련 API")
public class PlaceApiController {

    @Operation(summary = "장소 등록", description = "새로운 흡연 장소를 등록합니다")
    @ApiResponse(responseCode = "200", description = "등록 성공")
    @PostMapping("/api/v1/places")
    public ResponseEntity<Long> save(
        @Parameter(description = "장소 정보") @RequestPart PlaceSaveRequestDto requestDto,
        @Parameter(description = "장소 이미지들") @RequestPart List<MultipartFile> images
    ) {
        // 구현...
    }
}
```

### **4. 접속 URL**
```
개발 환경: http://localhost:8080/swagger-ui.html
운영 환경: https://smokingmap.duckdns.org/swagger-ui.html
```

---

## 📊 현재 프로젝트 적용 시 예상 결과

### **문서화될 API 수**
- 총 **9개 컨트롤러**
- 약 **30+ 개의 엔드포인트**
- 모든 DTO 클래스 자동 문서화

### **시간 절약 효과**
- API 스펙 문서 작성 시간: **0시간** (자동 생성)
- 프론트엔드 개발자 API 파악 시간: **90% 단축**  
- 신규 개발자 온보딩 시간: **50% 단축**

### **실제 사용 예시**
```
상황: 프론트엔드에서 장소 검색 API 사용하고 싶을 때

Before (문서화 전):
1. 백엔드 코드 뒤져서 엔드포인트 찾기 (10분)
2. DTO 클래스 찾아서 파라미터 확인 (5분)  
3. Postman으로 테스트 (5분)
4. 프론트엔드 코드 작성 (10분)
→ 총 30분

After (문서화 후):
1. Swagger 페이지 접속 (10초)
2. 검색 API 클릭해서 스펙 확인 (30초)
3. "Try it out"으로 바로 테스트 (1분)
4. 프론트엔드 코드 작성 (5분)  
→ 총 7분 (76% 시간 단축!)
```

이것이 바로 **API 문서화**의 핵심입니다! 🎯