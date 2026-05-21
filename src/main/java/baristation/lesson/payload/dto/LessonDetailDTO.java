package baristation.lesson.payload.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record LessonDetailDTO(
        Long lessonId,
        List<LessonImageDTO> lessonImages,
        String title,
        String hostName,
        String hostProfileUrl,
        String lessonCategory,
        String difficulty,
        List<String> careers,
        String region,
        String city,
        String place,
        List<LocalDateTime> schedules,
        Integer duration,
        List<CurriculumDTO> curriculum,
        Integer price,
        String lessonReservationUrl
) {
}
