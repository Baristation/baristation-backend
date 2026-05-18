package baristation.lesson.repository;

import baristation.lesson.domain.Lesson;
import baristation.lesson.payload.request.LessonSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LessonRepositoryCustom {

    // 검색 조건 + 페이징을 DB 레벨에서 처리하기 위한 커스텀 검색 메서드
    Page<Lesson> searchLessonsWithFilters(LessonSearchRequest request, Pageable pageable);
}
