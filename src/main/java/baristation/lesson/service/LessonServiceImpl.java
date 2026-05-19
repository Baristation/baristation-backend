package baristation.lesson.service;

import baristation.bean.enums.ImageType;
import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;
import baristation.common.payload.response.PageResponse;
import baristation.lesson.domain.Lesson;
import baristation.lesson.domain.LessonImage;
import baristation.lesson.domain.LessonSchedule;
import baristation.lesson.enums.ScheduleStatus;
import baristation.lesson.payload.dto.CurriculumDTO;
import baristation.lesson.payload.dto.LessonDTO;
import baristation.lesson.payload.dto.LessonDetailDTO;
import baristation.lesson.payload.dto.LessonImageDTO;
import baristation.lesson.payload.request.LessonSearchRequest;
import baristation.lesson.repository.LessonImageRepository;
import baristation.lesson.repository.LessonRepository;
import baristation.lesson.repository.LessonScheduleRepository;
import baristation.user.domain.Career;
import baristation.user.repository.CareerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Value("${cloudflare.r2.public-base-url}")
    private String publicBaseUrl;

    private final LessonRepository lessonRepository;
    private final LessonImageRepository lessonImageRepository;
    private final LessonScheduleRepository lessonScheduleRepository;
    private final CareerRepository careerRepository;

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

    // 추후 구현
    @Override
    public LessonDetailDTO getLessonDetail(Long lessonId) {

//        Lesson lesson = getLesson(lessonId);
//        List<LessonImageDTO> lessonImages = getLessonImages(lessonId);
//        List<String> careers = careerRepository.findByUser_UserId(lesson.getHostUser().getUserId())
//                .stream()
//                .map(Career::getTitle)
//                .toList();
//
//        List<LocalDateTime> schedules =
//        List<CurriculumDTO> curriculum
//        return LessonDetailDTO.bui

        return null;
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
                        // 레슨 이미지 DB 값은 objectKey이므로 API 응답에서는 전체 public URL로 변환합니다.
                        .imageUrl(buildImageUrl(image.getImageUrl()))
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
                        // 레슨 목록 썸네일도 응답 시 public URL prefix를 붙여 내려줍니다.
                        image -> buildImageUrl(image.getImageUrl()),
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
                .region(lesson.getRegion())
                .place(lesson.getPlace())
                .nextDate(nextSchedule == null ? null : nextSchedule.getLessonDate())
                .price(nextSchedule == null ? null : nextSchedule.getPrice())
                .difficulty(lesson.getDifficultyLevel())
                .hostProfileUrl(lesson.getHostUser().getProfileImageUrl())
                .build();
    }

    private String buildImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath;
        }

        String baseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;

        String path = imagePath.startsWith("/")
                ? imagePath
                : "/" + imagePath;

        return baseUrl + path;
    }
}
