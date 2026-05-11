package baristation.user.validator;

import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 닉네임 검증 클래스
 * - 길이: 2~20자
 * - 허용 문자: 한글(가-힣), 영문(a-zA-Z), 숫자(0-9), 언더바(_), 대시(-)
 * - 금지어: admin, administrator, root 등
 * - 특수문자 연속 사용 불가
 */
@Component
public class NicknameValidator {

    // 기본 정규표현식: 2~20자, 한글/영문/숫자/언더바/대시만 허용
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9가-힣]{2,20}$");
    // 예약어 목록
    private static final Set<String> RESERVED_WORDS = new HashSet<>();

    static {
        RESERVED_WORDS.add("admin");
        RESERVED_WORDS.add("administrator");
        RESERVED_WORDS.add("root");
        RESERVED_WORDS.add("support");
        RESERVED_WORDS.add("official");
        RESERVED_WORDS.add("baristation");
        RESERVED_WORDS.add("manager");
        RESERVED_WORDS.add("guest");
        RESERVED_WORDS.add("api");
        RESERVED_WORDS.add("login");
        RESERVED_WORDS.add("join");
        RESERVED_WORDS.add("anonymous");
    }

    /**
     * 닉네임 검증 (모든 규칙 적용)
     */
    public void validate(String nickname) {
        nickname = nickname.trim();
        if (nickname == null || nickname.isBlank()) {
            throw new CustomException(ErrorCode.USER_NICKNAME_REQUIRED);
        }

        // 1. 기본 형식 검증 (길이, 허용 문자)
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new CustomException(ErrorCode.USER_NICKNAME_INVALID_FORMAT);
        }
        // 2. 예약어 검증 (대소문자 무시)
        if (RESERVED_WORDS.contains(nickname.toLowerCase())) {
            throw new CustomException(ErrorCode.USER_NICKNAME_RESERVED);
        }
    }

    /**
     * 기본 형식만 검증 (내부 용도)
     */
    public boolean isValidFormat(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return false;
        }
        return NICKNAME_PATTERN.matcher(nickname).matches();
    }
}

