# Error Code Matrix (Auth/User)

## 공통 에러 응답 포맷

| 필드 | 타입 | 설명 |
|---|---|---|
| statusCode | string | `ErrorCode.code` 값 |
| message | string | `ErrorCode.message` 값 |
| data | object \| null | 에러 시 기본 `null` |

### 예시

```json
{
  "statusCode": "701-1",
  "message": "유효하지 않은 토큰입니다.",
  "data": null
}
```

---

## 인증/회원 API 에러코드 매트릭스

| API | HTTP | statusCode | message | 발생 조건 |
|---|---:|---|---|---|
| `POST /api/auth/refresh` | 400 | `701-4` | Refresh Token 헤더는 필수입니다. | `Refresh-Token` 누락/공백 |
| `POST /api/auth/refresh` | 401 | `701-1` | 유효하지 않은 토큰입니다. | 형식 오류, 서명 오류, 타입 불일치(refresh 아님), 블랙리스트 |
| `POST /api/auth/refresh` | 401 | `701-2` | 토큰이 만료되었습니다. | Refresh Token 만료 |
| `POST /api/auth/refresh` | 401 | `701-3` | Refresh Token이 일치하지 않거나 만료되었습니다. | Redis 저장 Refresh Token 불일치/없음 |
| `POST /api/auth/refresh` | 404 | `700-6` | 사용자를 찾을 수 없습니다. | 토큰 subject 기반 사용자 조회 실패 |
| `POST /api/auth/logout` | 401 | `701-1` | 유효하지 않은 토큰입니다. | Authorization 헤더 누락/형식 오류, 토큰 파싱 실패 |
| `DELETE /api/auth/delete` | 401 | `701-1` | 유효하지 않은 토큰입니다. | Authorization 헤더 누락/형식 오류, 토큰 파싱 실패 |
| `DELETE /api/auth/delete` | 404 | `700-6` | 사용자를 찾을 수 없습니다. | 사용자 조회 실패 |
| `PATCH /api/auth/update` | 401 | `701-1` | 유효하지 않은 토큰입니다. | Authorization 헤더 누락/형식 오류, 토큰 파싱 실패 |
| `PATCH /api/auth/update` | 404 | `700-6` | 사용자를 찾을 수 없습니다. | 사용자 조회 실패 |
| `PATCH /api/auth/update` | 400 | `700-7` | 닉네임은 필수 입력값입니다. | 요청 body null 또는 nickname null/blank |
| `PATCH /api/auth/update` | 400 | `700-7` | 닉네임은 필수 입력값입니다. | 도메인 `updateNickname()` 검증 실패(`IllegalArgumentException` 닉네임 메시지 매핑) |

---

## 필터/글로벌 fallback 매트릭스

| 처리 지점 | HTTP | statusCode | message | 비고 |
|---|---:|---|---|---|
| `JwtAuthenticationFilter` | 401 | `701-1` | 유효하지 않은 토큰입니다. | 보호 API access 토큰 검증 실패 |
| `JwtAuthenticationFilter` | 401 | `701-2` | 토큰이 만료되었습니다. | 보호 API access 토큰 만료 |
| `GlobalExceptionHandler` (`IllegalArgumentException`) | 400 | `900-3` | 잘못된 요청입니다. | 닉네임 관련 외 일반 `IllegalArgumentException` |
| `GlobalExceptionHandler` (`RuntimeException`/`Exception`) | 500 | `900-4` | 서버 내부 오류가 발생했습니다. | 현재 공통 500 fallback |

---

## 프론트 분기 권장 키

| 우선순위 | 기준 |
|---:|---|
| 1 | `statusCode` (문자열 코드 기준 분기) |
| 2 | HTTP status (401/404/400/500) |
| 3 | `message`는 UI 표시용, 분기 기준으로는 비권장 |

