package baristation.lesson.payload.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LessonDetailDTO(
        Long lessonId,
        List<LessonImageDTO> lessonImages,
        String title,
        String hostName,
        String hostProfileUrl,
        List<String> careers,
        String region,
        String city,
        String place,
        List<LocalDateTime> schedules,
        Integer duration,
        List<CurriculumDTO> curriculum,
        Integer price
) {
}
