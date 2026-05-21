package baristation.lesson.payload.dto;

import baristation.lesson.enums.DifficultyLevel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record LessonDTO(
        Long lessonId,
        String lessonImageUrl,
        String title,
        String subTitle,
        String hostName,
        String hostProfileUrl,
        String lessonCategory,
        String region,
        String place,
        LocalDateTime nextDate,
        Integer price,
        String difficulty
) {
}
