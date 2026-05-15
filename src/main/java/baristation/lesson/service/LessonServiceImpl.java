package baristation.lesson.service;

import baristation.bean.enums.ImageType;
import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;
import baristation.common.payload.response.PageResponse;
import baristation.lesson.domain.Lesson;
import baristation.lesson.domain.LessonImages;
import baristation.lesson.domain.LessonSchedule;
import baristation.lesson.enums.ScheduleStatus;
import baristation.lesson.payload.dto.LessonDTO;
import baristation.lesson.payload.dto.LessonDetailDTO;
import baristation.lesson.payload.request.LessonSearchRequest;
import baristation.lesson.repository.LessonImageRepository;
import baristation.lesson.repository.LessonRepository;
import baristation.lesson.repository.LessonScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final LessonImageRepository lessonImageRepository;
    private final LessonScheduleRepository lessonScheduleRepository;

    /**
     * 검색 조건에 맞는 클래스 목록을 조회하고, 썸네일 이미지와 가장 빠른 OPEN 일정을 함께 응답으로 조립
     */
    @Override
    public PageResponse<LessonDTO> searchLessons(LessonSearchRequest request, Pageable pageable) {
        validateSearchRequest(pageable);

        try {
            Page<Lesson> lessonPage = lessonRepository.searchLessonsWithFilters(request, pageable);
            if (lessonPage.isEmpty()) {
                return PageResponse.of(Page.empty(pageable));
            }

            List<Long> lessonIds = lessonPage.getContent().stream()
                    .map(Lesson::getLessonId)
                    .filter(Objects::nonNull)
                    .toList();

            Map<Long, String> thumbImageByLessonId = getThumbImages(lessonIds);
            Map<Long, LessonSchedule> nextScheduleByLessonId = getNextSchedules(lessonIds);

            Page<LessonDTO> page = new PageImpl<>(
                    lessonPage.getContent().stream()
                            .map(lesson -> toLessonDto(
                                    lesson,
                                    thumbImageByLessonId.get(lesson.getLessonId()),
                                    nextScheduleByLessonId.get(lesson.getLessonId())
                            ))
                            .toList(),
                    pageable,
                    lessonPage.getTotalElements()
            );

            return PageResponse.of(page);
        } catch (CustomException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new CustomException(ErrorCode.LESSON_SEARCH_FAILED);
        }
    }

    // 추후 구현
    @Override
    public LessonDetailDTO getLessonDetail(Long lessonId) {
        return null;
    }

    /**
     * 검색에 필요한 페이징 값이 유효한지 확인
     */
    private void validateSearchRequest(Pageable pageable) {
        if (pageable == null || pageable.getPageNumber() < 0 || pageable.getPageSize() < 1) {
            throw new CustomException(ErrorCode.LESSON_SEARCH_INVALID_REQUEST);
        }
    }

    /**
     * 현재 페이지에 포함된 클래스들의 첫 번째 썸네일 이미지를 조회
     */
    private Map<Long, String> getThumbImages(List<Long> lessonIds) {
        if (lessonIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return lessonImageRepository.findByLesson_LessonIdInAndImageTypeOrderBySortOrderAsc(lessonIds, ImageType.THUMB)
                .stream()
                .collect(Collectors.toMap(
                        image -> image.getLesson().getLessonId(),
                        LessonImages::getImageUrl,
                        (first, second) -> first
                ));
    }

    /**
     * 현재 페이지에 포함된 클래스들의 가장 빠른 OPEN 일정을 조회
     */
    private Map<Long, LessonSchedule> getNextSchedules(List<Long> lessonIds) {
        if (lessonIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return lessonScheduleRepository.findByLesson_LessonIdInAndScheduleStatusOrderByLessonDateAsc(
                        lessonIds,
                        ScheduleStatus.OPEN
                )
                .stream()
                .collect(Collectors.toMap(
                        schedule -> schedule.getLesson().getLessonId(),
                        schedule -> schedule,
                        (first, second) -> first
                ));
    }

    /**
     * 클래스 엔티티와 부가 조회 데이터를 목록 응답 DTO로 변환
     */
    private LessonDTO toLessonDto(Lesson lesson, String thumbImageUrl, LessonSchedule nextSchedule) {
        if (lesson == null || lesson.getLessonId() == null || lesson.getHostUser() == null) {
            throw new CustomException(ErrorCode.LESSON_SEARCH_MAPPING_FAILED);
        }

        return new LessonDTO(
                lesson.getLessonId(),
                thumbImageUrl,
                lesson.getTitle(),
                lesson.getSubtitle(),
                lesson.getHostUser().getNickname(),
                null,
                lesson.getRegion(),
                lesson.getPlace(),
                nextSchedule == null ? null : nextSchedule.getLessonDate(),
                nextSchedule == null ? null : nextSchedule.getPrice(),
                lesson.getDifficultyLevel()
        );
    }
}
