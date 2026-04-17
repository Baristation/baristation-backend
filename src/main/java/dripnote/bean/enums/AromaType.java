package dripnote.bean.enums;

/**
 * TODO 확정나지 않음
 */

public enum AromaType {
    FLORAL("꽃향기"),
    FRUITY("과일향"),
    NUTTY("견과류"),
    CHOCOLATY("초콜릿"),
    SPICY("향신료"),
    UNKNOWN("미지정"); // 아직 정의되지 않은 값을 위한 Fallback

    private final String description;
    AromaType(String description) { this.description = description; }
}