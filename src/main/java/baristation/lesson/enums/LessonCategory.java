package baristation.lesson.enums;

import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;

public enum LessonCategory {
    HOBBY("취미"),
    CERTIFICATE("자격증");

    private final String label;

    LessonCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static LessonCategory from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        for (LessonCategory category : values()) {
            if (
                    category.name().equalsIgnoreCase(normalized)
                            || category.label.equals(normalized)
            ) {
                return category;
            }
        }

        throw new CustomException(ErrorCode.LESSON_SEARCH_INVALID_REQUEST);
    }
}