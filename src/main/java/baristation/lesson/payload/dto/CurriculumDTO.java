package baristation.lesson.payload.dto;

import baristation.lesson.domain.LessonCurriculum;
import lombok.Builder;

@Builder
public record CurriculumDTO(
        String title,
        String summary,
        Integer sortOrder
) {
    public static CurriculumDTO from(LessonCurriculum lessonCurriculum) {
        return new CurriculumDTO(
                lessonCurriculum.getTitle(),
                lessonCurriculum.getSummary(),
                lessonCurriculum.getSortOrder()
        );
    }
}
