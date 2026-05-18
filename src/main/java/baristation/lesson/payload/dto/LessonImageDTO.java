package baristation.lesson.payload.dto;

import baristation.bean.enums.ImageType;
import lombok.Builder;

@Builder
public record LessonImageDTO(
        Long lessonImageId,
        ImageType imageType,
        String imageUrl,
        Integer sortOrder
) { }
