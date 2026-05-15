package baristation.lesson.payload.request;

import baristation.lesson.enums.DifficultyLevel;

public record LessonSearchRequest(
        String keyword,

        String region,
        DifficultyLevel difficulty
        ) {
}
