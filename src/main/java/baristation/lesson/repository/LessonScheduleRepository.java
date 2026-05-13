package baristation.lesson.repository;

import baristation.lesson.domain.LessonSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonScheduleRepository extends JpaRepository<LessonSchedule, Long> {
}
