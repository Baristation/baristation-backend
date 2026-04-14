1. DTO 전부 new -> 빌더 패턴으로 수정
2. Access Token과 Refresh Token이 동일한 claims/검증 로직으로 생성되어서, Refresh Token을 Authorization: Bearer로 보내면 일반 요청 인증에도 그대로 통과할 수 있습니다(만료 시간만 긴 access token처럼 동작). 토큰에 type(access/refresh) 같은 클레임을 추가하고, JwtAuthenticationFilter에서는 access 토큰만 허용하도록 구분해 주세요.
3. refresh()에서 jwtTokenProvider.validateToken(refreshToken)을 boolean 체크로 사용하고 있는데, validateToken은 만료/서명 오류 시 예외를 던질 수 있습니다. 현재 메서드에서 이를 CustomException(ErrorCode.TOKEN_*)로 변환하지 않으면 컨트롤러/글로벌 핸들러에서 의도치 않게 500으로 처리될 수 있으니, JWT 예외를 잡아서 CustomException으로 매핑하거나 validateToken의 동작을 boolean-only로 맞춰주세요.
4. validateToken()은 boolean을 반환하는 메서드인데, 아래 catch 블록에서 예외를 다시 던지고 있어 호출부가 if (!validateToken(...)) 형태로 쓰기 어렵습니다. 토큰 검증 API는 (1) 항상 boolean 반환 또는 (2) 항상 예외 던짐 중 하나로 계약을 통일해 주세요.
5. validateToken()에서 만료 토큰(ExpiredJwtException)은 예외를 그대로 던집니다. 이 메서드를 boolean 체크로 사용하는 서비스/컨트롤러에서는 해당 예외가 500으로 이어질 수 있으니, 만료/서명오류도 false로 처리하거나 호출부에서 명시적으로 예외를 CustomException(ErrorCode.TOKEN_EXPIRED 등)로 변환해 주세요.
6. updateUser 실패 시 400 + TOKEN_INVALID를 반환하고 있는데, 이 분기는 토큰 문제라기보다 (닉네임 미입력/유저 없음 등) 요청 검증 또는 리소스 미존재일 가능성이 큽니다. 실패 원인에 맞는 ErrorCode로 분리해서 클라이언트가 올바르게 처리할 수 있게 해 주세요.
7. 닉네임 생성이 existsByNickname 체크 + Math.random() 재시도로만 되어 있어 동시 로그인 요청에서는 레이스 컨디션으로 중복 닉네임이 저장될 수 있습니다(현재 nickname에 DB 유니크 제약도 없음). DB 유니크 제약 추가 + 저장 시 충돌 예외 처리(재시도)로 원자성을 보장해 주세요.
8. getEmail()에서 kakao_account가 없으면 kakaoAccount.get("email") 호출로 NPE가 발생합니다. kakaoAccount == null 체크를 먼저 하거나 안전한 널 처리를 추가해 주세요.
9. RuntimeException을 전부 AES_CIPHER_ERROR로 매핑하면 실제 원인과 무관한 에러 코드가 내려가서 클라이언트/운영에서 원인 파악이 어렵습니다. 공통 500용 ErrorCode를 별도로 두거나 예외 타입별로 적절한 ErrorCode로 매핑해 주세요.(ErrorCode 명확하게 하기)
10. @RequestHeader(value = "Refresh-Token")는 기본이 required=true라 헤더가 없으면 메서드 진입 전에 400이 발생합니다. 아래 null/blank 체크를 의도대로 동작시키려면 required = false로 바꾸거나, 헤더 누락을 전역 예외 처리로 일원화해 주세요.