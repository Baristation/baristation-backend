package baristation.lesson.repository;

import baristation.lesson.domain.LessonSchedule;
import baristation.lesson.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonScheduleRepository extends JpaRepository<LessonSchedule, Long> {
    List<LessonSchedule> findByLesson_LessonIdInAndScheduleStatusOrderByLessonDateAsc(List<Long> lessonIds, ScheduleStatus scheduleStatus);
}
