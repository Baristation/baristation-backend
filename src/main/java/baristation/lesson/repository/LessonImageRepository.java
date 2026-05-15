package baristation.lesson.repository;

import baristation.bean.enums.ImageType;
import baristation.lesson.domain.LessonImages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonImageRepository extends JpaRepository<LessonImages, Long> {
    List<LessonImages> findByLesson_LessonIdInAndImageTypeOrderBySortOrderAsc(List<Long> lessonIds, ImageType imageType);
}
