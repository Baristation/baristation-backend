package baristation.lesson.service;

import baristation.bean.enums.ImageType;
import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;
import baristation.common.payload.response.PageResponse;
import baristation.common.r2.ImageUrlResolver;
import baristation.lesson.domain.Lesson;
import baristation.lesson.domain.LessonCurriculum;
import baristation.lesson.domain.LessonImage;
import baristation.lesson.domain.LessonSchedule;
import baristation.lesson.enums.Region;
import baristation.lesson.enums.ScheduleStatus;
import baristation.lesson.payload.dto.CurriculumDTO;
import baristation.lesson.payload.dto.LessonDTO;
import baristation.lesson.payload.dto.LessonDetailDTO;
import baristation.lesson.payload.dto.LessonImageDTO;
import baristation.lesson.payload.request.LessonSearchRequest;
import baristation.lesson.repository.LessonCurriculumRepository;
import baristation.lesson.repository.LessonImageRepository;
import baristation.lesson.repository.LessonRepository;
import baristation.lesson.repository.LessonScheduleRepository;
import baristation.user.domain.Career;
import baristation.user.repository.CareerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static baristation.common.exception.ErrorCode.LESSON_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final LessonImageRepository lessonImageRepository;
    private final LessonCurriculumRepository lessonCurriculumRepository;
    private final LessonScheduleRepository lessonScheduleRepository;
    private final CareerRepository careerRepository;
    private final ImageUrlResolver imageUrlResolver;

    /**
     * 검색 조건에 맞는 클래스 목록을 조회하고, 썸네일 이미지와 가장 빠른 OPEN 일정을 함께 응답으로 조립
     */
    @Override
    public PageResponse<LessonDTO> searchLessons(LessonSearchRequest request, Pageable pageable) {
        validateSearchRequest(pageable);

        try {
            Page<Lesson> lessonPage = lessonRepository.searchLessonsWithFilters(request, pageable);

            List<Long> lessonIds = lessonPage.getContent().stream()
                    .map(Lesson::getLessonId)
                    .filter(Objects::nonNull)
                    .toList();

            Map<Long, String> thumbImageByLessonId = getThumbImages(lessonIds);
            Map<Long, LessonSchedule> nextScheduleByLessonId = getNextSchedules(lessonIds);

            // content가 비어 있어도 lessonPage.getTotalElements()는 그대로 유지
            // 마지막 페이지를 넘어선 요청에서 검색 결과 총 개수가 0으로 바뀌는 문제를 막기 위함
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

    @Override
    public LessonDetailDTO getLessonDetail(Long lessonId) {

        Lesson lesson = getLesson(lessonId);

        List<LessonImageDTO> lessonImages = getLessonImages(lessonId);

        List<String> careers = careerRepository.findByUser_UserId(lesson.getHostUser().getUserId())
                .stream()
                .map(Career::getTitle)
                .toList();

        List<LessonSchedule> lessonSchedules = getLessonSchedules(lessonId);

        // 시작 일시 목록
        List<LocalDateTime> schedules = lessonSchedules.stream()
                .map(this::toScheduleDateTime)
                .toList();

        // DTO로 변환하고 sortOrder 기준으로 정렬
        List<CurriculumDTO> curriculum = getCurriculum(lessonId);

        // 상세의 duration과 price는 가장 빠른 일정의 값을 대표값으로 사용
        LessonSchedule firstSchedule = lessonSchedules.stream()
                .findFirst()
                .orElse(null);

        return LessonDetailDTO.builder()
                .lessonId(lesson.getLessonId())
                .lessonImages(lessonImages)
                .title(lesson.getTitle())
                .hostName(lesson.getHostUser().getNickname())
                .hostProfileUrl(imageUrlResolver.toPublicUrl(lesson.getHostUser().getProfileImageUrl()))
                .lessonCategory(lesson.getLessonCategory().label())
                .careers(careers)
                .region(lesson.getRegion().label())
                .city(lesson.getCity())
                .place(lesson.getPlace())
                .schedules(schedules)
                .duration(firstSchedule == null ? null : firstSchedule.getDuration())
                .curriculum(curriculum)
                .price(firstSchedule == null ? null : firstSchedule.getPrice())
                .build();
    }

    // 해당 lesson의 전체 일정을 가져온 뒤, lessonDate와 startTime을 합쳐서 실제 시작 시각 기준으로 정렬
    private List<LessonSchedule> getLessonSchedules(Long lessonId) {
        return lessonScheduleRepository.findAll()
                .stream()
                .filter(schedule -> isScheduleOfLesson(schedule, lessonId))
                .sorted(Comparator.comparing(this::toScheduleDateTime))
                .toList();
    }

    private boolean isScheduleOfLesson(LessonSchedule schedule, Long lessonId) {
        return schedule.getLesson() != null
                && lessonId.equals(schedule.getLesson().getLessonId());
    }

    private List<CurriculumDTO> getCurriculum(Long lessonId) {
        return lessonCurriculumRepository.findAll()
                .stream()
                .filter(lessonCurriculum -> isCurriculumOfLesson(lessonCurriculum, lessonId))
                .map(CurriculumDTO::from)
                .sorted(Comparator.comparing(CurriculumDTO::sortOrder))
                .toList();
    }

    private boolean isCurriculumOfLesson(LessonCurriculum lessonCurriculum, Long lessonId) {
        return lessonCurriculum.getLesson() != null
                && lessonId.equals(lessonCurriculum.getLesson().getLessonId());
    }

    // lessonDate 컬럼의 날짜와 startTime 컬럼의 시간을 합쳐 실제 수업 시작 일시로 만듬
    private LocalDateTime toScheduleDateTime(LessonSchedule schedule) {
        if (schedule.getLessonDate() == null || schedule.getStartTime() == null) {
            return schedule.getLessonDate();
        }

        return schedule.getLessonDate().toLocalDate().atTime(schedule.getStartTime());
    }

    // 클래스 이미지 조회
    @Transactional(readOnly = true)
    public List<LessonImageDTO> getLessonImages(Long lessonId) {
        getLesson(lessonId);

        return lessonImageRepository.findByLesson_LessonIdOrderBySortOrderAsc(lessonId)
                .stream()
                .map(image -> LessonImageDTO.builder()
                        .lessonImageId(image.getLessonImageId())
                        .imageType(image.getImageType())
                        // 레슨 이미지 DB 값은 objectKey이므로 공통 컴포넌트로 public URL을 조립합니다.
                        .imageUrl(imageUrlResolver.toPublicUrl(image.getImageUrl()))
                        .sortOrder(image.getSortOrder())
                        .build())
                .toList();
    }

    // 클래스 존재 여부 확인
    private Lesson getLesson(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CustomException(LESSON_NOT_FOUND));
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
                        // 레슨 목록 썸네일도 공통 컴포넌트로 public URL prefix를 붙여 내려줍니다.
                        image -> imageUrlResolver.toPublicUrl(image.getImageUrl()),
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

        // 다음 일정은 현재 시각 이후에 열려 있는 일정만 후보로 본다.
        return lessonScheduleRepository.findByLesson_LessonIdInAndScheduleStatusAndLessonDateGreaterThanEqualOrderByLessonDateAsc(
                        lessonIds,
                        ScheduleStatus.OPEN,
                        LocalDateTime.now()
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

        return LessonDTO.builder()
                .lessonId(lesson.getLessonId())
                .lessonImageUrl(thumbImageUrl)
                .title(lesson.getTitle())
                .subTitle(lesson.getSubtitle())
                .hostName(lesson.getHostUser().getNickname())
                .region(lesson.getRegion().label())
                .lessonCategory(lesson.getLessonCategory().label())
                .place(lesson.getPlace())
                .nextDate(nextSchedule == null ? null : nextSchedule.getLessonDate())
                .price(nextSchedule == null ? null : nextSchedule.getPrice())
                .difficulty(lesson.getDifficultyLevel())
                .hostProfileUrl(lesson.getHostUser().getProfileImageUrl())
                .build();
    }
}
