# JWT 예외 처리 분석

## 📌 핵심 결론
**현재 방식이 정답입니다!** ✅

---

## 🔄 비교: 이전 vs 개선된 방식

### 1️⃣ Catch 순서 개선

#### 이전 순서 (문제점)
```
1. ExpiredJwtException
2. CustomException
3. 기타 JWT 예외
❌ 예상치 못한 에러 미처리
```

#### 개선된 순서 (최적화)
```
1. CustomException ← 가장 구체적
2. ExpiredJwtException
3. SecurityException, MalformedJwtException, UnsupportedJwtException, IllegalArgumentException
4. Exception ← 예상치 못한 모든 에러 방어
```

**이유**: 더 구체적인 예외를 먼저 catch하면 각 블록이 정확히 의도한 예외만 처리

---

## 🎯 3가지 핵심 개선 사항

### 개선 1️⃣: Catch 순서 정렬
```java
// ❌ 이전: 순서가 논리적이지 않음
catch (ExpiredJwtException e) { ... }
catch (CustomException e) { ... }
catch (SecurityException | ...) { ... }

// ✅ 개선: 구체 → 일반 순서
catch (CustomException e) { ... }
catch (ExpiredJwtException e) { ... }
catch (SecurityException | ...) { ... }
catch (Exception e) { ... }
```

### 개선 2️⃣: CustomException 명시적 재throw
```java
catch (CustomException e) {
    // 의도를 명확하게 표현
    // (코드 리더가 "이 예외는 처리되고 다시 던져진다"는 의도를 이해)
    throw e;
}
```

### 개선 3️⃣: 예상치 못한 에러 방어
```java
catch (Exception e) {
    // 필터에서 모든 예외를 안전하게 처리
    log.error("Unexpected error in JWT filter", e);
    sendJsonErrorResponse(response, ErrorCode.COMMON_INTERNAL_ERROR);
    return;
}
```

**장점**:
- 예외가 Controller까지 도달하지 않음 (보안)
- 필터에서 모든 경우를 안전하게 처리
- 예상치 못한 에러도 사용자에게 일관된 응답 제공

---

## 📊 상세 비교표

| 항목 | 이전 방식 | 개선된 방식 |
|------|---------|-----------|
| **Catch 순서** | 비논리적 ❌ | 논리적 ✅ |
| **CustomException 처리** | 암묵적 | 명시적 |
| **예상치 못한 에러** | 미처리 ⚠️ | 안전 처리 ✅ |
| **Logging** | 없음 | 있음 ✅ |
| **보안성** | 좋음 | 우수 ✅ |
| **유지보수성** | 양호 | 우수 ✅ |

---

## 💪 개선의 결과

### 안전성 (Safety)
- ✅ 모든 예외가 필터에서 처리됨
- ✅ 예외가 컨트롤러까지 도달하지 않음
- ✅ 런타임 에러 방지

### 명확성 (Clarity)
- ✅ Catch 블록의 의도가 명확함
- ✅ 어떤 예외가 어디서 처리되는지 쉽게 이해됨
- ✅ 코드 가독성 향상

### 방어성 (Defensive)
- ✅ 예상 범위 밖의 예외도 처리
- ✅ 서버 에러 로깅 추가
- ✅ 프로덕션 환경에서의 안정성 증대

---

## 🔍 예외 흐름도

```
JWT 검증 요청
    ↓
try {
    validateAccessToken(token)
    getAuthentication(token)
}
    ↓
┌─────────────────────────────────────────────┐
│         예외 발생 가능성                      │
├─────────────────────────────────────────────┤
│ 1. CustomException (Provider에서 던짐)      │
│    - TOKEN_INVALID                          │
│    - TOKEN_EXPIRED (변환됨)                │
│    - 기타 비즈니스 예외                     │
├─────────────────────────────────────────────┤
│ 2. ExpiredJwtException (라이브러리)         │
│    - 만료된 토큰                            │
├─────────────────────────────────────────────┤
│ 3. JWT 라이브러리 예외                      │
│    - SecurityException                      │
│    - MalformedJwtException                 │
│    - UnsupportedJwtException               │
│    - IllegalArgumentException              │
├─────────────────────────────────────────────┤
│ 4. 예상치 못한 예외                         │
│    - NullPointerException                  │
│    - IOException                           │
│    - 기타 모든 예외                         │
└─────────────────────────────────────────────┘
    ↓
catch 블록에서 적절히 처리
    ↓
sendJsonErrorResponse() → JSON 응답
    ↓
return (필터 체인 중단)
```

---

## 📝 권장사항

1. **현재 코드 유지** ✅
   - 이미 좋은 구조입니다

2. **로깅 추가 고려** 💡
   ```java
   catch (Exception e) {
       log.warn("Unexpected error in JWT filter", e);
       sendJsonErrorResponse(response, ErrorCode.COMMON_INTERNAL_ERROR);
       return;
   }
   ```

3. **모니터링** 📊
   - 예상치 못한 예외 발생 시 알림 설정

---

## ✨ 최종 정리

| 개선점 | 효과 |
|-------|------|
| 명시적인 Catch 순서 | 코드 가독성 및 유지보수성 ⬆️ |
| CustomException 재throw | 의도 명확화 |
| 일반 Exception catch | 보안 및 안정성 ⬆️ |
| Logging 추가 | 디버깅 용이성 ⬆️ |

**결론: 더 안전하고, 더 명확하고, 더 방어적인 코드** 🎯

