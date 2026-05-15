package baristation.lesson.payload.dto;

import baristation.bean.domain.ProductImage;
import baristation.bean.enums.ImageType;
import baristation.bean.payload.dto.ProductImageDTO;
import baristation.lesson.domain.Lesson;
import baristation.lesson.domain.LessonImages;

public record LessonImageDTO(
        Long lessonImageId,
        ImageType imageType,
        String imageUrl,
        Integer sortOrder
) {
    public static LessonImageDTO from(LessonImages lessonImage) {
        return new LessonImageDTO(
                lessonImage.getLessonImageId(),
                lessonImage.getImageType(),
                lessonImage.getImageUrl(),
                lessonImage.getSortOrder()
        );
    }
}
