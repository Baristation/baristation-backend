package baristation.lesson.enums;

import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;

public enum DifficultyLevel {
    BEGINNER("입문"),
    INTERMEDIATE("중급"),
    ADVANCED("고급");

    private final String label;

    DifficultyLevel(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static DifficultyLevel from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        for (DifficultyLevel difficultyLevel : values()) {
            if (
                    difficultyLevel.name().equalsIgnoreCase(normalized)
                            || difficultyLevel.label.equals(normalized)
            ) {
                return difficultyLevel;
            }
        }

        throw new CustomException(ErrorCode.LESSON_SEARCH_INVALID_REQUEST);
    }
}