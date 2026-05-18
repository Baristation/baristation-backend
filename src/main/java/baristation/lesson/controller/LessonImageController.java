package baristation.lesson.controller;

import baristation.common.logging.TraceIdUtil;
import baristation.common.payload.response.ApiResponse;
import baristation.lesson.payload.response.LessonImageResponse;
import baristation.lesson.service.LessonImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lessons")
@Slf4j
public class LessonImageController {

    private final LessonImageService lessonImageService;

    // 클래스 이미지 목록 조회
    @GetMapping("/{lessonId}/images")
    public ResponseEntity<ApiResponse<List<LessonImageResponse>>> getImages(@PathVariable("lessonId") Long lessonId) {
        log.info("[LessonImage] getImages start. lessonId={}, traceId={}", lessonId, TraceIdUtil.getTraceId());
        List<LessonImageResponse> response = lessonImageService.getImages(lessonId);
        log.info("[LessonImage] getImages done. lessonId={}, count={}, traceId={}",
                lessonId, response.size(), TraceIdUtil.getTraceId());
        return ApiResponse.ok(response);
    }

    // 대표 이미지 업로드 또는 교체
    @PostMapping(value = "/{lessonId}/images/thumb", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LessonImageResponse>> uploadThumb(
            @PathVariable("lessonId") Long lessonId,
            @RequestPart("file") MultipartFile file
    ) {
        log.info("[LessonImage] uploadThumb start. lessonId={}, fileSize={}, traceId={}",
                lessonId, file.getSize(), TraceIdUtil.getTraceId());
        LessonImageResponse response = lessonImageService.uploadThumb(lessonId, file);
        log.info("[LessonImage] uploadThumb done. lessonId={}, imageId={}, traceId={}",
                lessonId, response.lessonImageId(), TraceIdUtil.getTraceId());
        return ApiResponse.ok(response);
    }

    // 서브 이미지 추가
    @PostMapping(value = "/{lessonId}/images/sub", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LessonImageResponse>> uploadSub(
            @PathVariable("lessonId") Long lessonId,
            @RequestPart("file") MultipartFile file
    ) {
        log.info("[LessonImage] uploadSub start. lessonId={}, fileSize={}, traceId={}",
                lessonId, file.getSize(), TraceIdUtil.getTraceId());
        LessonImageResponse response = lessonImageService.uploadSub(lessonId, file);
        log.info("[LessonImage] uploadSub done. lessonId={}, imageId={}, traceId={}",
                lessonId, response.lessonImageId(), TraceIdUtil.getTraceId());
        return ApiResponse.ok(response);
    }

    // 특정 서브 이미지 교체
    @PutMapping(value = "/{lessonId}/images/{lessonImageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LessonImageResponse>> updateImage(
            @PathVariable("lessonId") Long lessonId,
            @PathVariable("lessonImageId") Long lessonImageId,
            @RequestPart("file") MultipartFile file
    ) {
        log.info("[LessonImage] updateImage start. lessonId={}, lessonImageId={}, fileSize={}, traceId={}",
                lessonId, lessonImageId, file.getSize(), TraceIdUtil.getTraceId());
        LessonImageResponse response = lessonImageService.updateImage(lessonId, lessonImageId, file);
        log.info("[LessonImage] updateImage done. lessonId={}, lessonImageId={}, traceId={}",
                lessonId, lessonImageId, TraceIdUtil.getTraceId());
        return ApiResponse.ok(response);
    }

    // 특정 이미지 삭제
    @DeleteMapping("/{lessonId}/images/{lessonImageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable("lessonId") Long lessonId,
            @PathVariable("lessonImageId") Long lessonImageId
    ) {
        log.info("[LessonImage] deleteImage start. lessonId={}, lessonImageId={}, traceId={}",
                lessonId, lessonImageId, TraceIdUtil.getTraceId());
        lessonImageService.deleteImage(lessonId, lessonImageId);
        log.info("[LessonImage] deleteImage done. lessonId={}, lessonImageId={}, traceId={}",
                lessonId, lessonImageId, TraceIdUtil.getTraceId());
        return ApiResponse.ok();
    }
}
