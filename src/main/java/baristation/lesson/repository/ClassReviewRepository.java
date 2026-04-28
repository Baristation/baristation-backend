package baristation.lesson.repository;

import baristation.lesson.domain.ClassReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassReviewRepository extends JpaRepository<ClassReview, Long> {
}
