package baristation.lesson.payload.response;

import baristation.bean.enums.ImageType;
import baristation.lesson.domain.LessonImage;
import lombok.Builder;

@Builder
public record LessonImageResponse(
        Long lessonImageId,
        Long lessonId,
        ImageType imageType,
        String imageUrl,
        Integer sortOrder
) {

    public static LessonImageResponse from(LessonImage lessonImage) {
        return new LessonImageResponse(
                lessonImage.getLessonImageId(),
                lessonImage.getLesson().getLessonId(),
                lessonImage.getImageType(),
                lessonImage.getImageUrl(),
                lessonImage.getSortOrder()
        );
    }
}