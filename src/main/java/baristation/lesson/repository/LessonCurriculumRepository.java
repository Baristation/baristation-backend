package baristation.lesson.repository;

import baristation.lesson.domain.LessonCurriculum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonCurriculumRepository extends JpaRepository<LessonCurriculum, Long> {

}