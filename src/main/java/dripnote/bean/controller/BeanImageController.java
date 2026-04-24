package dripnote.bean.controller;

import dripnote.bean.payload.response.BeanImageResponse;
import dripnote.bean.service.BeanImageServiceImpl;
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

    private final BeanImageServiceImpl beanImageService;

    // 원두(Product) 이미지 목록 조회
    @GetMapping("/{productId}/images")
    public ResponseEntity<List<BeanImageResponse>> getImages(@PathVariable Long productId) {
        List<BeanImageResponse> response = beanImageService.getImages(productId);
        return ResponseEntity.ok(response);
    }

    // 대표 이미지 업로드 또는 교체
    @PostMapping(value = "/{productId}/images/thumb", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BeanImageResponse> uploadThumb(
            @PathVariable Long productId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        BeanImageResponse response = beanImageService.uploadThumb(productId, file);
        return ResponseEntity.ok(response);
    }

    // 서브 이미지 추가
    @PostMapping(value = "/{productId}/images/sub", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BeanImageResponse> uploadSub(
            @PathVariable Long productId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        BeanImageResponse response = beanImageService.uploadSub(productId, file);
        return ResponseEntity.ok(response);
    }

    // 특정 서브 이미지 교체
    @PutMapping(value = "/{productId}/images/{beanImageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BeanImageResponse> updateImage(
            @PathVariable Long beanImageId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(beanImageService.updateImage(beanImageId, file));
    }

    // 특정 이미지 삭제
    @DeleteMapping("/{productId}/images/{beanImageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long beanImageId
    ) {
        beanImageService.deleteImage(beanImageId);
        return ResponseEntity.noContent().build();
    }
}