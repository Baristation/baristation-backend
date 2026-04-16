package dripnote.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    CONCURRENCY_CONFLICT(HttpStatus.CONFLICT, "100-1","다른 사용자가 먼저 수정했습니다. 페이지를 새로고침 후 다시 이용해주세요"),

    // 600xx: Bean 관련 오류
    BEAN_NOT_FOUND(HttpStatus.NOT_FOUND, "600-1", "원두를 찾을 수 없습니다."),
    CLUB_INFORMATION_NOT_FOUND(HttpStatus.NOT_FOUND, "600-2", "동아리 상세 정보가 존재하지 않습니다."),
    CLUB_FEED_IMAGES_NOT_FOUND(HttpStatus.NOT_FOUND, "600-3", "동아리 피드가 존재하지 않습니다."),
    CLUB_ID_INVALID(HttpStatus.BAD_REQUEST, "600-4", "올바르지 않은 클럽 요청입니다."),
    CLUB_SEARCH_FAILED(HttpStatus.BAD_REQUEST, "600-5", "검색 중 오류가 발생했습니다."),
    CLUB_DIVISION_INVALID(HttpStatus.BAD_REQUEST, "600-6", "올바르지 않은 분과입니다."),
    CLUB_CATEGORY_INVALID(HttpStatus.BAD_REQUEST, "600-7", "올바르지 않은 분류입니다."),
    TOO_MANY_TAGS(HttpStatus.BAD_REQUEST, "600-8", "태그는 최대 3개까지 입력할 수 있습니다."),
    TOO_LONG_TAG(HttpStatus.BAD_REQUEST, "600-9", "태그는 최대 5글자까지 입력할 수 있습니다."),
    TOO_LONG_INTRODUCTION(HttpStatus.BAD_REQUEST, "600-10", "소개는 최대 24글자까지 입력할 수 있습니다."),
    CLUB_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "600-11", "이미 사용 중인 동아리 이름입니다."),

    // 601xx: Class 관련 오류
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "601-1", "이미지 업로드에 실패하였습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "601-2", "이미지 파일을 찾을 수 없습니다."),
    IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "601-4", "이미지 삭제에 실패하였습니다"),
    KOREAN_FILE_NAME(HttpStatus.INTERNAL_SERVER_ERROR, "601-5", "파일명의 한국어를 인코딩할 수 없습니다."),
    FILE_TRANSFER_ERROR(HttpStatus.BAD_REQUEST, "601-6", "파일을 올바른 형식으로 변경할 수 없습니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "601-7", "파일의 확장자가 올바르지 않습니다."),
    INVALID_FILE_URL(HttpStatus.BAD_REQUEST, "601-8", "올바르지 않은 파일 URL입니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "601-9", "파일 삭제에 실패하였습니다."),
    WEBHOOK_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "601-11", "웹훅 요청이 올바르지 않습니다."),

     // 700xx: 사용자/권한 관련 오류
     USER_INVALID_LOGIN(HttpStatus.BAD_REQUEST, "700-4", "올바르지 않은 로그인"),
     USER_UNAUTHORIZED(HttpStatus.FORBIDDEN, "700-5", "권한이 없습니다."),
     USER_NOT_FOUND(HttpStatus.NOT_FOUND, "700-6", "사용자를 찾을 수 없습니다."),
     USER_NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "700-7", "닉네임은 필수 입력값입니다."),
     USER_NICKNAME_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "700-8", "닉네임은 2~20자의 한글, 영문, 숫자, 언더바(_), 대시(-)만 사용 가능합니다."),
     USER_NICKNAME_RESERVED(HttpStatus.BAD_REQUEST, "700-9", "사용할 수 없는 예약어입니다."),
     USER_NICKNAME_DUPLICATE(HttpStatus.CONFLICT, "700-10", "이미 사용 중인 닉네임입니다."),
     USER_NICKNAME_INVALID_SPECIAL_CHAR(HttpStatus.BAD_REQUEST, "700-11", "특수문자가 연속되거나 시작/끝에 위치할 수 없습니다."),

    // 701xx: 토큰 관련 오류
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "701-1", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "701-2", "토큰이 만료되었습니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "701-3", "Refresh Token이 일치하지 않거나 만료되었습니다."),
    REFRESH_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "701-4", "Refresh Token 헤더는 필수입니다."),

    // 702xx: 비밀번호 정책
    PASSWORD_SAME_AS_USERID(HttpStatus.BAD_REQUEST, "702-1", "아이디와 동일한 비밀번호는 설정할 수 없습니다."),
    PASSWORD_SAME_AS_OLD(HttpStatus.BAD_REQUEST,"702-2","이전 비밀번호와 동일한 비밀번호는 설정할 수 없습니다."),

    // 800xx: 이미지 관련
    INVALID_IMAGE_URL(HttpStatus.BAD_REQUEST, "800-1", "잘못된 imageUrl 형식입니다."),
    EMPTY_IMAGE_FILE(HttpStatus.BAD_REQUEST, "800-2", "빈 파일은 업로드할 수 없습니다."),
    UNSUPPORTED_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "800-3", "지원하지 않는 이미지 형식입니다."),
    IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "800-4", "이미지 크기는 5MB 이하여야 합니다."),
    BEAN_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "800-6", "원두 이미지를 찾을 수 없습니다."),
    THUMB_IMAGE_UPDATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "800-7", "대표 이미지는 전용 API를 사용해주세요."),

    // 900xx: 기타 시스템 오류
    AES_CIPHER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "900-1", "암호화 중 오류가 발생했습니다."),
    APPLICANT_NOT_FOUND(HttpStatus.NOT_FOUND, "900-2", "지원서가 존재하지 않습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "900-3", "잘못된 요청입니다."),
    COMMON_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "900-4", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
