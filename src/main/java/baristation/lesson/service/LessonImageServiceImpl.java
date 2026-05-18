package baristation.lesson.service;

import baristation.bean.enums.ImageType;
import baristation.common.exception.CustomException;
import baristation.common.r2.R2ImageService;
import baristation.lesson.domain.Lesson;
import baristation.lesson.domain.LessonImage;
import baristation.lesson.payload.response.LessonImageResponse;
import baristation.lesson.repository.LessonImageRepository;
import baristation.lesson.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static baristation.common.exception.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LessonImageServiceImpl implements LessonImageService {

    private final LessonRepository lessonRepository;
    private final LessonImageRepository lessonImageRepository;
    private final R2ImageService r2ImageService;

    private static final int THUMB_SORT_ORDER = 0;

    // 대표 이미지 업로드 또는 교체
    public LessonImageResponse uploadThumb(Long lessonId, MultipartFile file) throws IOException {
        Lesson lesson = getLesson(lessonId);

        LessonImage thumbImage = lessonImageRepository.findByLesson_LessonIdAndImageType(lessonId, ImageType.THUMB)
                .orElse(null);

        // 대표 이미지가 없으면 새로 저장
        if (thumbImage == null) {
            String imageUrl = r2ImageService.uploadLessonThumb(file, lessonId);

            LessonImage saved = lessonImageRepository.save(
                    LessonImage.builder()
                            .lesson(lesson)
                            .imageType(ImageType.THUMB)
                            .imageUrl(imageUrl)
                            .sortOrder(THUMB_SORT_ORDER)
                            .build()
            );

            return LessonImageResponse.builder()
                    .lessonImageId(saved.getLessonImageId())
                    .lessonId(saved.getLesson().getLessonId())
                    .imageType(ImageType.THUMB)
                    .imageUrl(imageUrl)
                    .sortOrder(THUMB_SORT_ORDER)
                    .build();
        }

        // 대표 이미지가 있으면 기존 URL 경로 재사용이 아니라 항상 새 규칙 경로로 업로드
        String oldImageUrl = thumbImage.getImageUrl();

        String newImageUrl = r2ImageService.uploadLessonThumb(file, lessonId);

        // 예전 구버전 경로였다면 정리
        // 이미 새 경로였다면 uploadLessonThumb()가 같은 위치를 덮어썼을 수 있으므로 삭제하면 안 됨
        if (!oldImageUrl.equals(newImageUrl)) {
            deletePreviousImageIfManaged(oldImageUrl);
        }

        thumbImage.changeImageUrl(newImageUrl);
        thumbImage.changeSortOrder(THUMB_SORT_ORDER);

        return LessonImageResponse.builder()
                .lessonImageId(thumbImage.getLessonImageId())
                .lessonId(thumbImage.getLesson().getLessonId())
                .imageType(thumbImage.getImageType())
                .imageUrl(thumbImage.getImageUrl())
                .sortOrder(thumbImage.getSortOrder())
                .build();
    }

    // 서브 이미지 추가
    public LessonImageResponse uploadSub(Long lessonId, MultipartFile file) throws IOException {
        Lesson lesson = getLesson(lessonId);

        int nextSortOrder = lessonImageRepository.findMaxSubSortOrder(lessonId) + 1;

        String imageUrl = r2ImageService.uploadLessonSubImage(file, lessonId);

        LessonImage subImage = LessonImage.builder()
                .lesson(lesson)
                .imageType(ImageType.SUB)
                .imageUrl(imageUrl)
                .sortOrder(nextSortOrder)
                .build();

        LessonImage saved = lessonImageRepository.save(subImage);

        return LessonImageResponse.builder()
                .lessonImageId(saved.getLessonImageId())
                .lessonId(saved.getLesson().getLessonId())
                .imageType(saved.getImageType())
                .imageUrl(saved.getImageUrl())
                .sortOrder(saved.getSortOrder())
                .build();
    }

    // 서브 이미지 교체
    public LessonImageResponse updateImage(Long lessonId, Long lessonImageId, MultipartFile file) throws IOException {
        LessonImage lessonImage = lessonImageRepository.findById(lessonImageId)
                .orElseThrow(() -> new CustomException(LESSON_IMAGE_NOT_FOUND));
        validateLessonImageOwner(lessonImage, lessonId);

        // 대표 이미지는 uploadThumb()을 통해 수정
        if (lessonImage.getImageType() == ImageType.THUMB) {
            throw new CustomException(THUMB_IMAGE_UPDATE_NOT_ALLOWED);
        }

        String oldImageUrl = lessonImage.getImageUrl();
        String newImageUrl = r2ImageService.uploadLessonSubImage(file, lessonId);

        // 기존 파일 삭제
        deletePreviousImageIfManaged(oldImageUrl);

        lessonImage.changeImageUrl(newImageUrl);

        return LessonImageResponse.builder()
                .lessonImageId(lessonImage.getLessonImageId())
                .lessonId(lessonImage.getLesson().getLessonId())
                .imageType(lessonImage.getImageType())
                .imageUrl(lessonImage.getImageUrl())
                .sortOrder(lessonImage.getSortOrder())
                .build();
    }

    // 이미지 삭제
    public void deleteImage(Long lessonId, Long lessonImageId) {
        LessonImage lessonImage = lessonImageRepository.findById(lessonImageId)
                .orElseThrow(() -> new CustomException(LESSON_IMAGE_NOT_FOUND));
        validateLessonImageOwner(lessonImage, lessonId);

        boolean isSubImage = lessonImage.getImageType() == ImageType.SUB;

        deletePreviousImageIfManaged(lessonImage.getImageUrl());
        lessonImageRepository.delete(lessonImage);

        if (isSubImage) {
            normalizeSubSortOrder(lessonId);
        }
    }

    // 클래스 이미지 조회
    @Transactional(readOnly = true)
    public List<LessonImageResponse> getImages(Long lessonId) {
        getLesson(lessonId);

        return lessonImageRepository.findByLesson_LessonIdOrderBySortOrderAsc(lessonId)
                .stream()
                .map(image -> LessonImageResponse.builder()
                        .lessonImageId(image.getLessonImageId())
                        .lessonId(image.getLesson().getLessonId())
                        .imageType(image.getImageType())
                        .imageUrl(image.getImageUrl())
                        .sortOrder(image.getSortOrder())
                        .build())
                .toList();
    }

    // 클래스 존재 여부 확인
    private Lesson getLesson(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CustomException(LESSON_NOT_FOUND));
    }

    // 서브 이미지 정렬 재조정
    private void normalizeSubSortOrder(Long lessonId) {
        List<LessonImage> subImages = lessonImageRepository
                .findByLesson_LessonIdAndImageTypeOrderBySortOrderAsc(lessonId, ImageType.SUB);

        for (int i = 0; i < subImages.size(); i++) {
            subImages.get(i).changeSortOrder(i + 1);
        }
    }

    private void validateLessonImageOwner(LessonImage lessonImage, Long lessonId) {
        if (!lessonImage.getLesson().getLessonId().equals(lessonId)) {
            throw new CustomException(LESSON_IMAGE_NOT_FOUND);
        }
    }

    private void deletePreviousImageIfManaged(String imageUrl) {
        try {
            r2ImageService.deleteByUrl(imageUrl);
        } catch (CustomException e) {
            if (e.getErrorCode() != INVALID_IMAGE_URL) {
                throw e;
            }
            log.warn("Skip deleting unmanaged lesson image URL. imageUrl={}", imageUrl);
        }
    }
}
