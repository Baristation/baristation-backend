# 원두 목록/상세 페이지 구현 상태 및 추가 할 일

## 📋 구현 완료 현황

### ✅ 완료된 것

| 구분 | 요구사항 | 상태 |
|------|---------|------|
| **목록 API** | `/api/beans/search` 엔드포인트 | ✅ 구현 |
| **상세 API** | `/api/beans/{beanId}` 엔드포인트 | ✅ 구현 |
| **목록 응답** | `BeanSummaryDTO` (원두명, 영문명, 생산지, 가공방식, 이미지) | ✅ 구현 |
| **상세 응답** | `BeanDetailDTO` (로스터리, 로스팅, 향미, 맛 수치, 이미지, 추천) | ✅ 구현 |
| **검색/필터** | 키워드, 향미, 맛 범위, 로스팅, 바디 필터링 | ✅ 구현 |
| **정렬** | 최신순, 이름순, 로스팅, 산미, 바디 | ✅ 구현 |
| **추천** | 유사 원두 추천 (RelatedBeanDTO) | ✅ 구현 |

---

## 🚀 추가로 해야 할 점

### 1️⃣ 예외 처리 & 정보 없음 표시

#### 1-1. 404 응답 처리
- [ ] `getBeanDetail()`에서 `beanId` 없을 때 처리 필요
  - 현재: `null` 반환 → **에러 응답**으로 변경 필요
  - 구현 위치: `BeanController.getBeanDetail()` / `BeanServiceImpl.getBeanDetail()`

#### 1-2. null 필드 처리 표준화
- [ ] `description`, `acidity`, `balance` 등이 `null`일 때 처리 방식 결정
  - **옵션 A**: `null` 그대로 반환 (클라이언트에서 처리)

#### 1-3. roaster 정보 없을 때 처리
- [ ] `product.getRoaster()` 가 `null`일 때 → `RoasterDTO`를 `null`로 반환하거나 기본값 설정
  - 위치: `BeanServiceImpl.getBeanDetail()` 라인 127

---

### 2️⃣ HTTP 테스트 파일 보강

**파일:** `src/test/java/com/Dripnote/user/bean-list.http`

추가할 테스트 케이스:

#### 3-1. 상세 조회 - 정상
```http request
### 13. 상세 조회: 정상 (productId 1)
GET http://localhost:8080/api/beans/1
```

#### 3-2. 상세 조회 - 없는 ID
```http request
### 14. 상세 조회: 존재하지 않는 ID
GET http://localhost:8080/api/beans/9999
```

#### 3-3. 상세 조회 - 정보 부족한 원두
```http request
### 15. 상세 조회: 정보 부족한 원두 (null 필드 테스트)
GET http://localhost:8080/api/beans/2
```

#### 3-4. 상세 조회 - 추천 원두 포함
```http request
### 16. 상세 조회: 추천 원두 확인
GET http://localhost:8080/api/beans/1
```

---

### 3⃣ BeanDetailDTO 최적화

#### 4-1. 이미지 정렬 순서 확인
- [ ] `images` 필드의 `sortOrder` 기준 정렬 검증
  - 위치: `BeanServiceImpl.getBeanDetail()` 라인 108-111
  - 확인: `productImageRepository.findByProduct_ProductIdOrderBySortOrderAsc()` 동작 확인

#### 4-2. relatedBeans 추천 로직 검증
- [ ] 현재 추천 점수 계산 로직 (scoreRelated 메서드)
  - 로스팅 타입 일치: +3점
  - 공통 향미: +5점
  - 산미 값: +점수 추가
  
**개선 여부 검토:**
- 점수 가중치 조정 필요한가?
- 추천 개수 4개가 적절한가?
- 다른 정렬 기준이 필요한가? (예: 인기도, 평점)

#### 4-3. 빈 추천 리스트 처리
- [ ] 유사 원두가 없을 때 빈 리스트 반환 → 클라이언트 처리 필요

---

### 5️⃣ 응답 일관성 검증

#### 5-1. BeanSummaryDTO 필드 일치 확인
```
목록 응답 BeanSummaryDTO:
- beanId, beanNameKo, beanNameEn, origin, region, process, productImage

상세 응답 내 BeanSummaryDTO:
- 동일한 구조인지 확인
```

- [ ] 필드 일치 여부 확인
- [ ] 타입 일치 여부 확인

#### 5-2. ProductImageDTO 일관성
```
sortOrder 필드가 추가됨:
- null일 수도 있음 → 기본값 처리 필요?
- 정렬 순서 적용 여부 확인
```

- [ ] `sortOrder` null 처리
- [ ] 이미지 정렬이 의도대로 동작하는지 확인

---

### 6️⃣ 페이지네이션/성능 고려 (선택사항)

#### 6-1. 현재 구조
- 오프셋 기반 페이지네이션 (page, size)
- 상태 유지: 검색/필터/정렬 쿼리 파라미터로 유지

#### 6-2. 개선 옵션
- [ ] 커서 기반 페이지네이션 필요한가?
  - 장점: 대용량 데이터에서 성능 좋음
  - 단점: 구현 복잡도 증가
  
- [ ] N+1 문제 점검
  - 현재 `@EntityGraph` 사용 중
  - 추가 최적화 필요한가?

---

## 🔍 우선순위별 추천 작업 순서

### **즉시 필요** (1순위)
1. **예외 처리**: `getBeanDetail()`에서 없는 `beanId` 처리 → 404 또는 Optional 반환
2. **HTTP 테스트**: 상세 API 케이스 추가 (정상, 404, null 필드 포함)
3. **검증**: 정보 없음 케이스 테스트 (null 값 처리 검증)

**예상 소요 시간:** 1-2시간

### **기능 완성** (2순위)
4. **필드 추가 결정**: 블렌딩, 품종, 고도 필요 여부 확인
5. **추천 로직 개선**: `relatedBeans` 알고리즘 검증 및 개선
6. **응답 일관성**: 모든 DTO 필드 검증

**예상 소요 시간:** 2-3시간

### **최적화** (3순위)
7. **성능 개선**: N+1 최적화, 커서 페이지네이션 검토
8. **에러 핸들링**: 전역 예외 처리 추가
9. **문서화**: API 문서 업데이트

**예상 소요 시간:** 2-4시간

---

## 📝 체크리스트

### 1순위
- [ ] `BeanController.getBeanDetail()` 예외 처리
- [ ] `BeanServiceImpl.getBeanDetail()` null 처리
- [ ] HTTP 테스트 파일 상세 API 케이스 추가
- [ ] 정보 없음 필드 처리 표준화

### 2순위
- [ ] 부족한 필드 필요 여부 결정
- [ ] `relatedBeans` 추천 로직 검증
- [ ] 응답 DTO 필드 일치 확인

### 3순위
- [ ] 페이지네이션 개선 검토
- [ ] 성능 최적화
- [ ] API 문서 작성

---

## 📌 현재 엔드포인트

### 목록 조회
```
GET /api/beans/search
Query Params: keyword, flavorCategory, minAcidity, maxAcidity, minSweetness, maxSweetness, minBitterness, maxBitterness, body, roastingType, sortBy, page, size
Response: PageResponse<BeanSummaryDTO>
```

### 상세 조회
```
GET /api/beans/{beanId}
Path Params: beanId
Response: BeanDetailDTO
Status: 200 OK (정상), 404 Not Found (없는 경우) - 추가 필요
```

---

## 📌 구현 세부 파일 위치

| 항목 | 위치 |
|------|------|
| 컨트롤러 | `src/main/java/dripnote/bean/controller/BeanController.java` |
| 서비스 인터페이스 | `src/main/java/dripnote/bean/service/BeanService.java` |
| 서비스 구현 | `src/main/java/dripnote/bean/service/BeanServiceImpl.java` |
| DTO | `src/main/java/dripnote/bean/payload/dto/` |
| 요청 | `src/main/java/dripnote/bean/payload/request/BeanSearchRequest.java` |
| HTTP 테스트 | `src/test/java/com/Dripnote/user/bean-list.http` |

---

**작성일:** 2026-04-26  
**상태:** 기본 구현 완료, 예외 처리 및 최적화 필요

