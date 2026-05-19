package baristation.lesson.repository;

import baristation.lesson.domain.LessonSchedule;
import baristation.lesson.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LessonScheduleRepository extends JpaRepository<LessonSchedule, Long> {
    List<LessonSchedule> findByLesson_LessonIdInAndScheduleStatusAndLessonDateGreaterThanEqualOrderByLessonDateAsc(
            List<Long> lessonIds,
            ScheduleStatus scheduleStatus,
            LocalDateTime lessonDate
    );
}