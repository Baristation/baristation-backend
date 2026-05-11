package baristation.bean.controller;

import baristation.bean.payload.response.BeanImageResponse;
import baristation.bean.service.BeanImageServiceImpl;
import baristation.common.logging.TraceIdUtil;
import baristation.common.payload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/beans")
@Slf4j
public class ProductImageController {

    private final BeanImageServiceImpl beanImageService;

    // 원두 이미지 목록 조회
    @GetMapping("/{productId}/images")
    public ResponseEntity<ApiResponse<List<BeanImageResponse>>> getImages(@PathVariable("productId") Long productId) {
        log.info("[BeanImage] getImages start. productId={}, traceId={}", productId, TraceIdUtil.getTraceId());
        List<BeanImageResponse> response = beanImageService.getImages(productId);
        log.info("[BeanImage] getImages done. productId={}, count={}, traceId={}",
                productId, response.size(), TraceIdUtil.getTraceId());
        return ApiResponse.ok(response);
    }

    // 대표 이미지 업로드 또는 교체
    @PostMapping(value = "/{productId}/images/thumb", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BeanImageResponse>> uploadThumb(
            @PathVariable("productId") Long productId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        log.info("[BeanImage] uploadThumb start. productId={}, fileSize={}, traceId={}",
                productId, file.getSize(), TraceIdUtil.getTraceId());
        BeanImageResponse response = beanImageService.uploadThumb(productId, file);
        log.info("[BeanImage] uploadThumb done. productId={}, imageId={}, traceId={}",
                productId, response.productImageId(), TraceIdUtil.getTraceId());
        return ApiResponse.ok(response);
    }

    // 서브 이미지 추가
    @PostMapping(value = "/{productId}/images/sub", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BeanImageResponse>> uploadSub(
            @PathVariable("productId") Long productId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        log.info("[BeanImage] uploadSub start. productId={}, fileSize={}, traceId={}",
                productId, file.getSize(), TraceIdUtil.getTraceId());
        BeanImageResponse response = beanImageService.uploadSub(productId, file);
        log.info("[BeanImage] uploadSub done. productId={}, imageId={}, traceId={}",
                productId, response.productImageId(), TraceIdUtil.getTraceId());
        return ApiResponse.ok(response);
    }

    // 특정 서브 이미지 교체
    @PutMapping(value = "/{productId}/images/{beanImageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BeanImageResponse>> updateImage(
            @PathVariable("productId") Long productId,
            @PathVariable("beanImageId") Long beanImageId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        log.info("[BeanImage] updateImage start. productId={}, beanImageId={}, fileSize={}, traceId={}",
                productId, beanImageId, file.getSize(), TraceIdUtil.getTraceId());
        BeanImageResponse response = beanImageService.updateImage(beanImageId, file);
        log.info("[BeanImage] updateImage done. productId={}, beanImageId={}, traceId={}",
                productId, beanImageId, TraceIdUtil.getTraceId());
        return ApiResponse.ok(response);
    }

    // 특정 이미지 삭제
    @DeleteMapping("/{productId}/images/{beanImageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable("productId") Long productId,
            @PathVariable("beanImageId") Long beanImageId
    ) {
        log.info("[BeanImage] deleteImage start. productId={}, beanImageId={}, traceId={}",
                productId, beanImageId, TraceIdUtil.getTraceId());
        beanImageService.deleteImage(beanImageId);
        log.info("[BeanImage] deleteImage done. productId={}, beanImageId={}, traceId={}",
                productId, beanImageId, TraceIdUtil.getTraceId());
        return ApiResponse.ok();
    }
}