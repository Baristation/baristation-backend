package baristation.bean.enums;

public enum RoastingType {
    LIGHT(1, "Light"),
    MEDIUMLIGHT(2, "MediumLight"),
    MEDIUM(3, "Medium"),
    MEDIUMDARK(4, "MediumDark"),
    DARK(5, "Dark");

    private final int code;
    private final String description;

    // 생성자에서 숫자와 설명을 모두 받음
    RoastingType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}