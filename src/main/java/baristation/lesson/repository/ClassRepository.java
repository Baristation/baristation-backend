package baristation.lesson.repository;

import baristation.lesson.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassRepository extends JpaRepository<Lesson, Long> {
}
