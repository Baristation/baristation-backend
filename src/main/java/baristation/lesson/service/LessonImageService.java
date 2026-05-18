package baristation.lesson.service;

import baristation.lesson.payload.response.LessonImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LessonImageService {
    // ==========================================
    // 2. 클래스 이미지 관리 (Lesson Image Management)
    // ==========================================

    /**
     * 상품의 대표(썸네일) 이미지를 업로드하거나 기존 이미지를 새 이미지로 교체합니다.
     */
    LessonImageResponse uploadThumb(Long lessonId, MultipartFile file) throws IOException;

    /**
     * 클래스의 서브 이미지를 새롭게 추가합니다. (가장 마지막 순서로 배정)
     */
    LessonImageResponse uploadSub(Long lessonId, MultipartFile file) throws IOException;

    /**
     * 기존에 등록된 서브 이미지를 다른 이미지 파일로 교체합니다.
     */
    LessonImageResponse updateImage(Long lessonId, Long lessonImageId, MultipartFile file) throws IOException;

    /**
     * 상품 이미지를 삭제합니다. 서브 이미지일 경우 남은 이미지들의 정렬 순서를 재조정합니다.
     */
    void deleteImage(Long lessonId, Long lessonImageId);

    /**
     * 특정 상품에 등록된 모든 이미지 목록을 정렬 순서(SortOrder)에 맞춰 조회합니다.
     */
    List<LessonImageResponse> getImages(Long lessonId);
}
