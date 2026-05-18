package baristation.lesson.repository;

import baristation.bean.enums.ImageType;
import baristation.lesson.domain.LessonImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LessonImageRepository extends JpaRepository<LessonImage, Long> {
    List<LessonImage> findByLesson_LessonIdInAndImageTypeOrderBySortOrderAsc(List<Long> lessonIds, ImageType imageType);
    List<LessonImage> findByLesson_LessonIdAndImageTypeOrderBySortOrderAsc(Long lessonId, ImageType imageType);
    List<LessonImage> findByLesson_LessonIdOrderBySortOrderAsc(Long lessonId);

    Optional<LessonImage> findByLesson_LessonIdAndImageType(Long lessonId, ImageType imageType);

    @Query("""
        select coalesce(max(bi.sortOrder), 0)
        from LessonImage bi
        where bi.lesson.lessonId = :lessonId
          and bi.imageType = ImageType.SUB
    """)
    Integer findMaxSubSortOrder(Long lessonId);
}
