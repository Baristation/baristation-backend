package baristation.lesson.enums;

import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;

public enum Region {
    SEOUL("서울"),
    GYEONGGI("경기"),
    INCHEON("인천"),
    BUSAN("부산"),
    DAEGU("대구"),
    GWANGJU("광주"),
    DAEJEON("대전"),
    ULSAN("울산"),
    SEJONG("세종"),
    GANGWON("강원"),
    CHUNGBUK("충북"),
    CHUNGNAM("충남"),
    JEONBUK("전북"),
    JEONNAM("전남"),
    GYEONGBUK("경북"),
    GYEONGNAM("경남"),
    JEJU("제주");

    private final String label;

    Region(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static Region from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        for (Region region : values()) {
            if (region.label.equals(normalized)) {
                return region;
            }
        }

        throw new CustomException(ErrorCode.LESSON_SEARCH_INVALID_REQUEST);
    }
}
