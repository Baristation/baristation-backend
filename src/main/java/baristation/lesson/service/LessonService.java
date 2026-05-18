package baristation.lesson.service;

import baristation.common.payload.response.PageResponse;
import baristation.lesson.payload.dto.LessonDTO;
import baristation.lesson.payload.dto.LessonDetailDTO;
import baristation.lesson.payload.request.LessonSearchRequest;
import org.springframework.data.domain.Pageable;

public interface LessonService {
    /**
     * 조건에 맞는 클래스 목록을 검색
     */
    PageResponse<LessonDTO> searchLessons(LessonSearchRequest request, Pageable pageable);

    /**
     * 클래스 단건 상세 조회
     */
    LessonDetailDTO getLessonDetail(Long lessonId);
}
