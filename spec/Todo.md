# ☕ Dripnote: 원두 목록 페이지 개발 전략 가이드

## 1. 핵심 설계 원칙: DB로부터의 독립
- **현황**: 다음 주 커피 관련 ERD 개편 예정.
- **전략**: Entity(DB)에 의존하지 않고 **DTO**와 **Interface**를 먼저 설계하여 프론트엔드와 소통할 수 있는 '규격'을 선점한다.

---

## 2. 데이터 구조 설계 (Enum & DTO)

### [AromaType Enum]
아직 세부 분류가 나오지 않았으므로, 커피 테이스팅의 대분류를 기준으로 우선 정의합니다.

```java
public enum AromaType {
    FLORAL, FRUITY, NUTTY, CHOCOLATY, HERBAL, SPICY, EARTHY, UNKNOWN
}
```

### [BeanSearchCondition DTO]
검색 및 필터링 조건을 담는 객체입니다.

```java
public record BeanSearchCondition(
    String keyword,        // 이름, 생산지 검색
    List<AromaType> aromas, // 다중 선택 필터링
    Integer minPrice,      // 가격 필터링 (필요 시)
    Integer maxPrice
) {}
```

### [BeanListResponse DTO]
목록 페이지에서 사용자에게 보여줄 데이터 규격입니다.

```java
public record BeanListResponse(
    Long id,
    String name,
    String origin,
    AromaType representativeAroma, // 대표 아로마
    String roastLevel,
    Integer price,
    String imageUrl
) {}
```

---

## 3. 개발 단계별 로드맵

### Phase 1: 규격 확립 및 Mock API (금주 진행)
1. **DTO & Enum 생성**: 위에서 정의한 규격을 프로젝트에 반영합니다.
2. **Service 인터페이스 정의**:
   ```java
   public interface BeanService {
       Page<BeanListResponse> getBeanList(BeanSearchCondition condition, Pageable pageable);
   }
   ```
3. **Mock 구현체 작성**: DB 연동 없이 `new BeanListResponse(...)`로 가짜 리스트 10개를 만들어 반환하도록 구현합니다.
4. **Controller 연결**: Postman으로 가짜 데이터가 잘 내려오는지 테스트합니다.

### Phase 2: 검색 엔진 구축 (차주 진행)
1. **Querydsl 환경 설정**: 동적 쿼리를 처리하기 위한 기본 설정을 마칩니다.
2. **Repository 구현**: 확정된 ERD를 바탕으로 `BeanRepositoryCustom`을 구현합니다.
3. **Pagination 최적화**: 데이터가 많아질 것에 대비해 `No-offset` 방식이나 `Count 쿼리 최적화`를 적용합니다.

---

## 4. 검색/필터링 로직 포인트 (Querydsl 예시)
아로마 필터링 시 `in` 절을 사용하여 성우님이 고민하신 '원자성' 있는 검색을 구현합니다.

```java
private BooleanExpression aromaIn(List<AromaType> aromas) {
    return (aromas == null || aromas.isEmpty()) ? null : bean.aroma.in(aromas);
}
```


## 5. 예상 질문 및 대비 (FAQ)
- **Q: 다음 주에 아로마가 테이블로 분리되면요?**
    - **A**: DTO는 그대로 두되, `BeanService` 내부의 `Repository` 호출 로직만 `join` 쿼리로 수정하면 됩니다. 컨트롤러와 프론트엔드 코드는 수정할 필요가 없습니다.
- **Q: 검색 성능은 어떻게 잡나요?**
    - **A**: 닉네임 중복 체크 때처럼 인덱스를 적절히 활용하고, Querydsl의 동적 쿼리를 통해 필요한 필드만 조회(Projection)하도록 최적화합니다.~~
