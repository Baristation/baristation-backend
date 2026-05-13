package baristation.lesson.repository;

import baristation.lesson.domain.LessonImages;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonImageRepository extends JpaRepository<LessonImages, Long> {
}
