package baristation.lesson.repository;

import baristation.lesson.domain.LessonReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonReviewRepository extends JpaRepository<LessonReview, Long> {
}
