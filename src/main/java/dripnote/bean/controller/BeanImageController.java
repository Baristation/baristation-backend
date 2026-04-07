package dripnote.bean.controller;

import dripnote.bean.payload.response.BeanImageResponse;
import dripnote.bean.service.BeanImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/beans")
public class BeanImageController {

    private final BeanImageService beanImageService;

    // 원두 이미지 목록 조회
    @GetMapping("/{beanId}/images")
    public ResponseEntity<List<BeanImageResponse>> getImages(@PathVariable Long beanId) {
        List<BeanImageResponse> response = beanImageService.getImages(beanId);
        return ResponseEntity.ok(response);
    }

    // 대표 이미지 업로드 또는 교체
    @PostMapping(value = "/{beanId}/images/thumb", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BeanImageResponse> uploadThumb(
            @PathVariable Long beanId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        BeanImageResponse response = beanImageService.uploadThumb(beanId, file);
        return ResponseEntity.ok(response);
    }

    // 서브 이미지 추가
    @PostMapping(value = "/{beanId}/images/sub", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BeanImageResponse> uploadSub(
            @PathVariable Long beanId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        BeanImageResponse response = beanImageService.uploadSub(beanId, file);
        return ResponseEntity.ok(response);
    }

    // 특정 서브 이미지 교체
    @PutMapping(value = "/{beanId}/images/{beanImageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BeanImageResponse> updateImage(
            @PathVariable Long beanId,
            @PathVariable Long beanImageId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(beanImageService.updateImage(beanImageId, file));
    }

    // 특정 이미지 삭제
    @DeleteMapping("/{beanId}/images/{beanImageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long beanId,
            @PathVariable Long beanImageId
    ) {
        beanImageService.deleteImage(beanImageId);
        return ResponseEntity.noContent().build();
    }
}