
# 🚀 실전 백엔드 로그 최적화 가이드 (Operational Logging Strategy)

## 1. 핵심 원칙: "Less is More, But Traceable"
*   **성공 로그는 메트릭으로:** "단순 성공" 횟수는 로그가 아닌 메트릭(Prometheus/Grafana)으로 관리합니다.
*   **로그는 경계(Boundary)에서만:** 로직 내부의 자잘한 진행 상황보다는 시스템의 입구와 출구, 외부 연동 지점만 기록합니다.
*   **추적 ID 필수:** 모든 로그에 `Trace-ID`를 부여하여 흩어진 로그를 하나의 흐름으로 묶습니다.

---

## 2. 로그 레벨 및 기록 기준 (Optimized)

| 레벨 | 기록 시점 (운영 서버 기준) | 비고 |
| :--- | :--- | :--- |
| **ERROR** | **즉시 대응이 필요한 실패** (5xx 에러, DB 연결 끊김 등) | 알림(Slack 등) 연동 필수 |
| **WARN** | **잠재적 위험** (재시도 후 성공, API 권장 기한 만료, 비정상적 파라미터) | 모니터링 대상 |
| **INFO** | **비즈니스 마일스톤** (API 진입/종료, 외부 호출 결과, 상태 확정 변경) | 최소한으로 유지 |
| **DEBUG** | 로컬/테스트 환경에서만 활성화 (상세 로직 분기 확인) | 운영 서버 출력 금지 |

---

## 3. 3대 최적화 로깅 규칙

### ① Boundary Logging (경계 기록)
모든 로직을 기록하지 말고, 아래 세 지점만 정확히 기록해도 90%의 장애를 잡을 수 있습니다.
*   **Inbound:** API 요청이 들어온 시점 (URL, Method, Trace-ID)
*   **Outbound:** 외부 API 호출 및 DB 쓰기 시점 (결과값 위주)
*   **Response:** API 응답을 돌려주는 시점 (상태 코드, 소요 시간)

### ② 로그 대신 메트릭 사용
*   **로그 금지:** `log.info("User {} login success", userId);` (초당 수천 건 발생 시 비용 폭발)
*   **메트릭 권장:** `counter.increment("login.success");`
*   **판단 기준:** "이 정보가 특정 유저의 흐름을 쫓기 위함인가(Log), 아니면 시스템의 상태를 보기 위함인가(Metric)?"

### ③ 구조화된 JSON 로깅
텍스트 형태의 로그는 검색이 어렵고 저장 용량만 차지합니다.
*   **Bad:** `[2026-04-30] INFO - Order 12345 changed to status SHIPPED`
*   **Good (JSON):**
  ```json
  {"level":"INFO", "type":"ORDER", "id":"12345", "status":"SHIPPED", "traceId":"abc-123"}
  
```

---

## 4. 실전 코드 가이드 (Java/Spring 예시)

```java
// 최적화된 로깅 예시
public void processOrder(OrderRequest request) {
    // 1. Inbound: TraceID는 인터셉터/필터에서 자동 생성되어 로그에 포함됨
    log.info("[Order] Start - orderId: {}, type: {}", request.getId(), request.getType());

    // 중략: 내부 로직(재고 확인, 가격 계산 등)은 로그를 남기지 않음 (필요 시 DEBUG)

    try {
        // 2. Outbound: 외부 시스템 연동 결과는 중요하므로 기록
        PaymentResult result = paymentClient.request(request.getAmount());
        log.info("[Order] Payment Finished - orderId: {}, result: {}", request.getId(), result.getCode());
    } catch (Exception e) {
        // 3. Exception: 에러는 맥락과 함께 ERROR 레벨로 상세히
        log.error("[Order] Process Failed - orderId: {}, reason: {}", request.getId(), e.getMessage(), e);
        throw e;
    }
}
```

---

## 5. 보안 및 관리 수칙
*   **민감 정보 마스킹:** 비밀번호, 카드번호, 개인정보는 절대 기록하지 않습니다.
*   **로그 보존 기간(TTL):** 운영 로그는 보통 **7~14일**만 보관하고, 장기 보관이 필요한 경우 저렴한 저장소(S3 등)로 아카이빙합니다.
*   **System.out 금지:** 반드시 로깅 프레임워크를 사용하고, 로그 출력 시 비동기(Async) 모드를 사용하여 메인 로직 성능에 영향을 주지 않도록 합니다.
