## Plan: 원두목록 페이지 백엔드 구현 기능 정리

요구사항을 분석하여 백엔드에서 구현해야 할 기능들을 **검색/필터링**, **정렬**, **페이지네이션** 3개 영역으로 정리합니다. 현재 코드베이스에 이미 기초가 있으므로, 이를 확장/완성하는 형태입니다.

### 백엔드 구현 범위 (4개 계층)

#### 1. API 요청 정의 (`BeanSearchRequest` 확장)
- [BeanSearchRequest](src/main/java/dripnote/bean/payload/request/BeanSearchRequest.java)에 **정렬** 필드 추가
  - `SortType sortBy` (최신순, 이름순, 로스팅정도, 산미순, 바디순)
  - 선택사항: 가공방식 필터 필드 추가 (`processType`)

#### 2. Repository 동적 쿼리 (QueryDSL 권장)
- `Product` / `Bean` / `ProductFlavorNote` 조인하여 **복합 검색/필터** 쿼리 구현
  - 키워드 검색: Bean명, Roaster명 (LIKE)
  - 향미 단일 필터: FlavorNote 태그
  - 로스팅타입 필터: `product.roastLevel`
  - 맛/바디 범위필터: `product.acidity`, `product.sweetness`, `product.body`, `product.balance`
  - 가공방식 필터 (선택): `bean.process` (구현 여부 확인 필요)

#### 3. Service 로직 (`BeanServiceImpl` 구현)
- `searchBeans(BeanSearchRequest, Pageable)` 완성 (현재 스텁)3
  - Repository 동적쿼리 호출로 필터링 데이터 조회
  - `BeanListItemDTO` 대량 조회 최적화 (N+1 회피)
  - 정렬 순서 적용 (`Pageable.getSort()` 또는 요청의 `sortBy`)

#### 4. Controller 엔드포인트 (`BeanController`)
- `GET /api/beans` 
  - 요청: `BeanSearchRequest` (쿼리파라미터/RequestBody), `Pageable` (page, size, sort)
  - 응답: `PageResponse<BeanListItemDTO>` (페이지 정보 + 목록)

---

### 구체적 구현 상세

| 기능 | 백엔드 담당 | 구현 대상 |
|------|-----------|---------|
| **실시간 검색** | 키워드 필터링 쿼리 | Repository (QueryDSL): `keyword` → Bean명/Roaster명 LIKE |
| **다중 필터링** | 향미, 로스팅, 맛/바디 | Repository: `aromas` (List) IN, `roastingType`, `minAcidity~maxBody` 범위 |
| **정렬** | 5가지 정렬옵션 | `BeanSearchRequest`에 `SortType` 필드 + Service에서 정렬 적용 |
| **페이지네이션** | 상태유지 | Spring `Pageable` (page, size) + 검색/필터/정렬 상태는 요청에 포함 |

---

### 추가 고려사항

1. **향미 필터링 구조 확인**
   - `ProductFlavorNote` 테이블로 Product와 FlavorNote 연관 → 조인 쿼리 필요
   - 또는 FlavorCategory로 그룹핑 가능 (현재 `AromaType`과의 관계 확인 필요)

2. **정렬 기본값**
   - 클라이언트가 sort 미지정 시 기본값 (최신순)

3. **성능 최적화**
   - 페이지당 로드할 이미지는 `ImageType.THUMB` 만 (이미 구현)
   - FlavorNote 대량 로드 시 `@EntityGraph` 활용 (이미 적용)

---

### 구현 Steps

1. `BeanSearchRequest`에 정렬 필드 추가 (`SortType sortBy`)
2. QueryDSL 기반 Repository 메서드 구현 (또는 `@Query` 동적쿼리) → 검색/필터/정렬 통합
3. `BeanServiceImpl.searchBeans()` 완성 → Repository 호출 + DTO 조립
4. `BeanController`에 `GET /api/beans` 엔드포인트 작성
5. (선택) 향미 필터링 로직 검증 및 정렬 기본값 결정

---

### 현재 코드 상태

- ✅ BeanService 인터페이스 & 기초 구현 (BeanServiceImpl)
- ✅ BeanListItemDTO 포함 DTO 레이어 완성
- ❌ Repository 동적쿼리 미구현 (QueryDSL 설정 필요)
- ❌ BeanController 엔드포인트 미구현 (stub 상태)
- ❌ BeanSearchRequest 정렬필드 추가 필요

