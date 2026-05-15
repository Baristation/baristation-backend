package baristation.lesson.payload.dto;

import baristation.lesson.enums.DifficultyLevel;

import java.time.LocalDateTime;

public record LessonDTO(
        Long lessonId,
        String lessonImageUrl,
        String title,
        String subTitle,
        String hostName,
        String hostProfileUrl,
        String region,
        String place,
        LocalDateTime nextDate,
        Integer price,
        DifficultyLevel difficulty
) {
}
